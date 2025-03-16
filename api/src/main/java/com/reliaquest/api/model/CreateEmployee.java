package com.reliaquest.api.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Data
public class CreateEmployee {

    private String name;
    private Integer salary;
    private Integer age;
    private String title;


}
