package org.iesalixar.daw2.josemiguelgarcialopez.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Province {

    private int id;

    private String code;

    private String name;

    private Region region;

    public Province(String code, String name, Region region){
        this.code = code;
        this.name = name;
        this.region = region;
    }

}
