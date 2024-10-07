package org.iesalixar.daw2.josemiguelgarcialopez.dao;

import org.iesalixar.daw2.josemiguelgarcialopez.entity.Province;
import org.iesalixar.daw2.josemiguelgarcialopez.entity.Region;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProvinceDAOImpl implements ProvinceDAO {

    /**
     * Lista todas las provincias junto con sus regiones.
     * @return List<Province> lista de provincias.
     * @throws SQLException en caso de error de conexión o ejecución SQL.
     */
    public List<Province> listAllProvinces() throws SQLException {
        List<Province> provinces = new ArrayList<>();
        // Consulta SQL para obtener provincias y sus regiones ordenadas por ID
        String query = "SELECT p.id AS id_province, p.code AS code_province, p.name AS name_province, " +
                "r.id AS id_region, r.code AS code_region, r.name AS name_region " +
                "FROM provinces p INNER JOIN regions r ON p.id_region = r.id " +
                "ORDER BY p.id"; // Agregar ORDER BY para asegurar el orden por ID

        // Ejecutar la consulta y procesar el resultado
        try (Connection connection = DatabaseConnectionManager.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                int id_province = resultSet.getInt("id_province");
                String code_province = resultSet.getString("code_province");
                String name_province = resultSet.getString("name_province");

                // Obtener datos de la región
                int id_region = resultSet.getInt("id_region");
                String code_region = resultSet.getString("code_region");
                String name_region = resultSet.getString("name_region");

                // Crear objetos Region y Province
                Region region = new Region(id_region, code_region, name_region);
                Province province = new Province(id_province, code_province, name_province, region);

                // Agregar la provincia a la lista
                provinces.add(province);
            }
        }

        return provinces; // Retornar la lista de provincias
    }

    /**
     * Inserta una nueva provincia en la base de datos.
     * @param province La provincia a insertar.
     * @throws SQLException en caso de error de conexión o ejecución SQL.
     */
    public void insertProvince(Province province) throws SQLException {
        // Consulta SQL para insertar una nueva provincia
        String query = "INSERT INTO provinces (code, name, id_region) VALUES (?, ?, ?)";

        // Preparar y ejecutar la consulta
        try (Connection connection = DatabaseConnectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, province.getCode());
            preparedStatement.setString(2, province.getName());
            preparedStatement.setInt(3, province.getRegion().getId());
            preparedStatement.executeUpdate(); // Ejecutar la actualización
        }
    }

    /**
     * Verifica si existe una provincia con el código especificado.
     * @param code El código de la provincia a verificar.
     * @return true si existe, false en caso contrario.
     * @throws SQLException en caso de error de conexión o ejecución SQL.
     */
    public boolean existsProvinceByCode(String code) throws SQLException {
        // Consulta SQL para verificar la existencia de una provincia por código
        String sql = "SELECT COUNT(*) FROM provinces WHERE UPPER(code) = ?";
        try (Connection connection = DatabaseConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, code.toUpperCase()); // Asegurarse de que la comparación sea insensible a mayúsculas
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next(); // Mover al primer resultado
                return resultSet.getInt(1) > 0; // Retornar true si existe al menos una provincia
            }
        }
    }

    /**
     * Actualiza los datos de una provincia existente en la base de datos.
     * @param province La provincia con los nuevos datos.
     * @throws SQLException en caso de error de conexión o ejecución SQL.
     */
    public void updateProvince(Province province) throws SQLException {
        // Consulta SQL para actualizar una provincia
        String query = "UPDATE provinces SET code = ?, name = ? WHERE id = ?";
        try (Connection connection = DatabaseConnectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, province.getCode());
            preparedStatement.setString(2, province.getName());
            preparedStatement.setInt(3, province.getId());
            preparedStatement.executeUpdate(); // Ejecutar la actualización
        }
    }

    /**
     * Elimina una provincia de la base de datos.
     * @param id ID de la provincia a eliminar.
     * @throws SQLException en caso de error de conexión o ejecución SQL.
     */
    public void deleteProvince(int id) throws SQLException {
        // Consulta SQL para eliminar una provincia
        String query = "DELETE FROM provinces WHERE id = ?";
        try (Connection connection = DatabaseConnectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, id); // Establecer el ID de la provincia a eliminar
            preparedStatement.executeUpdate(); // Ejecutar la eliminación
        }
    }

    /**
     * Obtiene una provincia por su ID.
     * @param id El ID de la provincia a obtener.
     * @return La provincia correspondiente o null si no se encuentra.
     * @throws SQLException en caso de error de conexión o ejecución SQL.
     */
    public Province getProvinceById(int id) throws SQLException {
        // Consulta SQL para obtener una provincia por ID
        String query = "SELECT p.id AS id_province, p.code AS code_province, p.name AS name_province, " +
                "r.id AS id_region, r.code AS code_region, r.name AS name_region " +
                "FROM provinces p " +
                "INNER JOIN regions r ON p.id_region = r.id " +
                "WHERE p.id = ?";
        Province province = null;

        try (Connection connection = DatabaseConnectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, id); // Establecer el ID de la provincia a buscar
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String code = resultSet.getString("code_province");
                String name = resultSet.getString("name_province");

                // Datos de la región
                int regionId = resultSet.getInt("id_region");
                String codeRegion = resultSet.getString("code_region");
                String nameRegion = resultSet.getString("name_region");

                // Crear objeto Region
                Region region = new Region(regionId, codeRegion, nameRegion);

                // Crear objeto Province
                province = new Province(id, code, name, region);
            }
        }
        return province; // Retornar la provincia encontrada o null
    }

    /**
     * Verifica si existe una provincia con el código especificado y un ID diferente.
     * @param code El código de la provincia a verificar.
     * @param id El ID de la provincia a ignorar en la búsqueda.
     * @return true si existe, false en caso contrario.
     * @throws SQLException en caso de error de conexión o ejecución SQL.
     */
    public boolean existsProvinceByCodeAndNotId(String code, int id) throws SQLException {
        // Consulta SQL para verificar la existencia de una provincia por código, ignorando el ID
        String sql = "SELECT COUNT(*) FROM provinces WHERE UPPER(code) = ? AND id != ?";
        try (Connection connection = DatabaseConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, code.toUpperCase());
            statement.setInt(2, id); // Establecer el ID a ignorar
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1) > 0; // Retornar true si existe al menos una provincia
            }
        }
    }
}
