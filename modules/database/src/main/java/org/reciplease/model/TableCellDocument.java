package org.reciplease.model;

import java.util.List;

public record TableCellDocument(
        String text, List<TableCellDocument> inputs, CellDurationDocument duration, Boolean handsOff) {

    public static TableCellDocument from(final TableCell cell) {
        return new TableCellDocument(
                cell.text(),
                cell.inputs().stream().map(TableCellDocument::from).toList(),
                CellDurationDocument.from(cell.duration()),
                cell.handsOff());
    }

    public TableCell toModel() {
        return new TableCell(
                text,
                inputs == null
                        ? List.of()
                        : inputs.stream().map(TableCellDocument::toModel).toList(),
                duration == null ? null : duration.toModel(),
                Boolean.TRUE.equals(handsOff));
    }
}
