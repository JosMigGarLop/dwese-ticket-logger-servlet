package org.iesalixar.daw2.josemiguelgarcialopez.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.iesalixar.daw2.josemiguelgarcialopez.dao.ProvinceDAO;
import org.iesalixar.daw2.josemiguelgarcialopez.dao.ProvinceDAOImpl;
import org.iesalixar.daw2.josemiguelgarcialopez.dao.RegionDAO;
import org.iesalixar.daw2.josemiguelgarcialopez.dao.RegionDAOImpl;
import org.iesalixar.daw2.josemiguelgarcialopez.entity.Province;
import org.iesalixar.daw2.josemiguelgarcialopez.entity.Region;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/provinces") // URL que se mapea a este servlet
public class ProvinceServlet extends HttpServlet {

    private ProvinceDAO provinceDAO; // DAO para operaciones de Provincia
    private RegionDAO regionDAO; // DAO para operaciones de Región

    @Override
    public void init() throws ServletException {
        try {
            // Inicializa los DAOs
            provinceDAO = new ProvinceDAOImpl();
            regionDAO = new RegionDAOImpl();
        } catch (Exception e) {
            throw new ServletException("Error al inicializar el ProvinceDAO", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        try {
            // Acción predeterminada
            if (action == null) {
                action = "list";
            }

            // Manejo de las distintas acciones
            switch (action) {
                case "new":
                    showNewForm(request, response); // Muestra el formulario para nueva provincia
                    break;
                case "edit":
                    showEditForm(request, response); // Muestra el formulario para editar provincia
                    break;
                default:
                    listProvinces(request, response); // Lista todas las provincias
                    break;
            }
        } catch (SQLException | IOException ex) {
            throw new ServletException(ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        try {
            // Manejo de las acciones POST
            switch (action) {
                case "insert":
                    insertProvince(request, response);  // Insertar nueva provincia
                    break;
                case "update":
                    updateProvince(request, response);  // Actualizar provincia existente
                    break;
                case "delete":
                    deleteProvince(request, response);  // Eliminar provincia
                    break;
                default:
                    listProvinces(request, response);   // Listar todas las provincias
                    break;
            }
        } catch (SQLException ex) {
            throw new ServletException(ex);
        }
    }

    private void listProvinces(HttpServletRequest request, HttpServletResponse response) throws SQLException, ServletException, IOException {
        // Obtiene la lista de provincias y la pasa a la vista
        List<Province> listProvinces = provinceDAO.listAllProvinces();
        request.setAttribute("listProvinces", listProvinces);
        request.getRequestDispatcher("province.jsp").forward(request, response);
    }

    private void showNewForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, SQLException {
        // Muestra el formulario para crear una nueva provincia
        List<Region> listRegions = regionDAO.listAllRegions(); // Obtiene la lista de regiones
        request.setAttribute("listRegions", listRegions); // Pasa la lista de regiones a la vista
        request.getRequestDispatcher("province-form.jsp").forward(request, response); // Redirige a la vista del formulario
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response) throws SQLException, ServletException, IOException {
        // Muestra el formulario para editar una provincia existente
        int id = Integer.parseInt(request.getParameter("id")); // Obtiene el ID de la provincia
        Province existingProvince = provinceDAO.getProvinceById(id); // Obtiene la provincia por ID
        request.setAttribute("province", existingProvince); // Pasa la provincia a la vista

        List<Region> listRegions = regionDAO.listAllRegions(); // Obtiene la lista de regiones
        request.setAttribute("listRegions", listRegions); // Pasa la lista de regiones a la vista
        request.getRequestDispatcher("province-form.jsp").forward(request, response); // Redirige a la vista del formulario
    }

    private void insertProvince(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException, ServletException {
        // Maneja la inserción de una nueva provincia
        String code = request.getParameter("code"); // Obtiene el código
        String name = request.getParameter("name"); // Obtiene el nombre
        String id_region = request.getParameter("id_region"); // Obtiene el ID de la región

        // Validaciones básicas
        if (code.isEmpty() || name.isEmpty() || id_region.isEmpty()) {
            request.setAttribute("errorMessage", "El código, el nombre y la comunidad autónoma no pueden estar vacíos.");

            // Cargar la lista de regiones
            List<Region> listRegions = regionDAO.listAllRegions(); // Obtiene la lista de regiones
            request.setAttribute("listRegions", listRegions); // Pasa la lista de regiones a la vista
            request.getRequestDispatcher("province-form.jsp").forward(request, response);
            return;
        }

        // Verifica si el código ya existe
        if (provinceDAO.existsProvinceByCode(code)) {
            request.setAttribute("errorMessage", "El código de la provincia ya existe.");

            // Cargar la lista de regiones
            List<Region> listRegions = regionDAO.listAllRegions(); // Obtiene la lista de regiones
            request.setAttribute("listRegions", listRegions); // Pasa la lista de regiones a la vista
            request.getRequestDispatcher("province-form.jsp").forward(request, response);
            return;
        }

        // Crea y configura el objeto Provincia
        Province province = new Province();
        province.setCode(code);
        province.setName(name);

        // Obtiene y configura la región
        Region region = regionDAO.getRegionById(Integer.parseInt(id_region));
        province.setRegion(region);

        try {
            // Inserta la nueva provincia
            provinceDAO.insertProvince(province);
        } catch (SQLException e) {
            // Maneja excepciones por violaciones de restricciones de unicidad
            if (e.getSQLState().equals("23505")) { // Código SQL para unique constraint violation
                request.setAttribute("errorMessage", "El código de la provincia debe ser único.");

                // Cargar la lista de regiones
                List<Region> listRegions = regionDAO.listAllRegions(); // Obtiene la lista de regiones
                request.setAttribute("listRegions", listRegions); // Pasa la lista de regiones a la vista
                request.getRequestDispatcher("province-form.jsp").forward(request, response);
            } else {
                throw e; // Relanza excepciones no manejadas
            }
        }

        // Redirige al listado de provincias
        response.sendRedirect("provinces");
    }


    private void updateProvince(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException, ServletException {
        // Maneja la actualización de una provincia existente
        int id = Integer.parseInt(request.getParameter("id")); // Obtiene el ID de la provincia
        String code = request.getParameter("code").trim().toUpperCase(); // Convierte a mayúsculas
        String name = request.getParameter("name").trim();
        String id_region = request.getParameter("id_region");

        // Validaciones básicas
        if (code.isEmpty() || name.isEmpty() || id_region.isEmpty()) {
            request.setAttribute("errorMessage", "El código, el nombre y la comunidad autónoma no pueden estar vacíos.");
            request.getRequestDispatcher("province-form.jsp").forward(request, response);
            return;
        }

        // Verifica si el código ya existe, ignorando el ID de la provincia actual
        if (provinceDAO.existsProvinceByCodeAndNotId(code, id)) {
            request.setAttribute("errorMessage", "El código de la provincia ya existe.");
            request.getRequestDispatcher("province-form.jsp").forward(request, response);
            return;
        }

        // Crea y configura el objeto Provincia
        Province province = new Province();
        province.setId(id); // Establecer el ID de la provincia
        province.setCode(code);
        province.setName(name);

        // Obtiene y configura la región
        Region region = regionDAO.getRegionById(Integer.parseInt(id_region));
        province.setRegion(region);

        try {
            // Actualiza la provincia
            provinceDAO.updateProvince(province); // Asegúrate de tener un método `updateProvince` en tu DAO
        } catch (SQLException e) {
            // Maneja excepciones por violaciones de restricciones de unicidad
            if (e.getSQLState().equals("23505")) { // Código SQL para unique constraint violation
                request.setAttribute("errorMessage", "El código de la provincia debe ser único.");
                request.getRequestDispatcher("province-form.jsp").forward(request, response);
            } else {
                throw e; // Relanza excepciones no manejadas
            }
        }

        // Redirige al listado de provincias
        response.sendRedirect("provinces");
    }

    private void deleteProvince(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        // Maneja la eliminación de una provincia
        int id = Integer.parseInt(request.getParameter("id")); // Obtiene el ID de la provincia
        provinceDAO.deleteProvince(id);  // Elimina la provincia usando el DAO
        response.sendRedirect("provinces"); // Redirige al listado de provincias
    }
}
