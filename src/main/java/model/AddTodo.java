package model;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class AddTodo {
     private String id;
     private String name;
     private String dateStart;
     private String dateEnd;
}
