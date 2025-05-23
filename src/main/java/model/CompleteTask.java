package model;

import lombok.*;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class CompleteTask extends AddTodo {
    private String id;
    private String name;
    private String status;
    private String startDate;
    private String deadline;
    private String completedDate;

}
