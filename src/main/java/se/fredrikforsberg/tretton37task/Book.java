package se.fredrikforsberg.tretton37task;

import lombok.Builder;
import lombok.Data;

import java.sql.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class Book {
    private String title;
    private String description;
    private List<Map.Entry> entries = new ArrayList<>();
}
