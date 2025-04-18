import { useEffect, useState } from "react";
import Button from "../../Button";

const DataList = ({ data = [], columns, renderHeader, renderCell, renderColumnCell, onSelect }) => {
    const [values, setValues] = useState([]);
    const [selectedRows, setSelectedRows] = useState(new Set());
    const [currentPage, setCurrentPage] = useState(1);
    const pageSize = 10;

    useEffect(() => {
        setValues(data);
    }, [data]);

    useEffect(() => {
        const selectedIds = Array.from(selectedRows)
            .map(index => values[index]?.id)
            .filter(Boolean);
        onSelect?.(selectedIds);
    }, [selectedRows, values, onSelect]);

    const headers = new Set();
    if (columns?.length > 0) {
        columns.forEach(col => headers.add(col));
    } else {
        values.forEach(item => Object.keys(item).forEach(key => headers.add(key)));
    }

    const totalPages = Math.ceil(values.length / pageSize);
    const paginatedData = values.slice((currentPage - 1) * pageSize, currentPage * pageSize);

    const handleRowSelect = (index, event) => {
        const globalIndex = (currentPage - 1) * pageSize + index;

        setSelectedRows(prevSelected => {
            const newSelection = new Set(prevSelected);
            if (event.ctrlKey || event.metaKey) {
                newSelection.has(globalIndex) ? newSelection.delete(globalIndex) : newSelection.add(globalIndex);
            } else {
                newSelection.clear();
                newSelection.add(globalIndex);
            }
            return newSelection;
        });
    };

    const handlePageChange = (newPage) => {
        setSelectedRows(new Set());
        setCurrentPage(newPage);
    };

    const renderColumnWithNestedData = (value, row, header) => {
        if (header === "completed") {
            return value ? "Завершено" : "Не завершено";
        }
        if (header === "age") {
            return String(value);
        }
        console.log(value)
        return value;
    };

    console.log(headers);

    return (
        <div>
            <table border="1" cellPadding="8" style={{ width: "100%", borderCollapse: "collapse" }}>
                <thead>
                <tr>
                    <th>#</th>
                    {Array.from(headers).map((header, index) => (
                        <th key={index}>
                            {renderHeader ? renderHeader(header) : header}
                        </th>
                    ))}
                </tr>
                </thead>
                <tbody>
                {paginatedData.length === 0 ? (
                    <tr>
                        <td colSpan={headers.size + 1} style={{ textAlign: "center" }}>
                            Нет данных
                        </td>
                    </tr>
                ) : (
                    paginatedData.map((row, rowIndex) => {
                        const globalIndex = (currentPage - 1) * pageSize + rowIndex;
                        return (
                            <tr
                                key={rowIndex}
                                style={{
                                    backgroundColor: selectedRows.has(globalIndex) ? "#cce5ff" : "white"
                                }}
                            >
                                <td
                                    style={{
                                        textAlign: "center",
                                        cursor: "pointer",
                                        userSelect: "none"
                                    }}
                                    onClick={(e) => handleRowSelect(rowIndex, e)}
                                >
                                    {selectedRows.has(globalIndex) ? "✅" : "⬜"}
                                </td>
                                {Array.from(headers).map((header, colIndex) => (
                                    <td key={colIndex}>
                                        {renderColumnCell?.[header]
                                            ? renderColumnCell[header](row[header], row)
                                            : renderColumnWithNestedData(row[header], row, header)}
                                    </td>
                                ))}
                            </tr>
                        );
                    })
                )}
                </tbody>
            </table>

            <div style={{ marginTop: "10px", display: "flex", justifyContent: "center", gap: "10px" }}>
                <Button onClick={() => handlePageChange(currentPage - 1)} disabled={currentPage === 1}>
                    Назад
                </Button>
                <span>Страница {currentPage} из {totalPages}</span>
                <Button onClick={() => handlePageChange(currentPage + 1)} disabled={currentPage === totalPages}>
                    Вперёд
                </Button>
            </div>
        </div>
    );
};

export default DataList;
