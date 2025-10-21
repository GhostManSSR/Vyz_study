import React, { useState } from "react";
import { DataGrid, GridColDef, GridRowParams, GridRowSelectionModel } from "@mui/x-data-grid";
import { Chip, Switch } from "@mui/material";

export interface UserData {
    id: number;
    name: string;
    email: string;
    roles: string[];
    status: boolean;
}

const columns = (onStatusChange: (id: number, checked: boolean) => void): GridColDef<UserData>[] => [
    { field: "name", headerName: "Name", flex: 1 },
    { field: "email", headerName: "Email", flex: 1 },
    {
        field: "roles",
        headerName: "Roles",
        flex: 1,
        renderCell: (params) => (
            <>
                {params.value.map((role) => (
                    <Chip key={role} label={role} size="small" color="primary" style={{ marginRight: 4 }} />
                ))}
            </>
        ),
    },
    {
        field: "status",
        headerName: "Status",
        flex: 0.5,
        renderCell: (params) => (
            <Switch
                checked={params.value}
                onChange={(event, checked) => {
                    onStatusChange(params.id as number, checked);
                }}
                color="primary"
            />
        ),
    },
];

interface UserTableProps {
    rows: UserData[];
    onUpdateStatus: (id: number, checked: boolean) => void;
}

export default function UserTable({ rows, onUpdateStatus }: UserTableProps) {
    const [selectionModel, setSelectionModel] = React.useState<GridRowSelectionModel>({
        type: "include",
        ids: new Set(),
    });

    const handleSelection = (model: GridRowSelectionModel) => {
        setSelectionModel(model);
    };

    const handleRowClick = (params: GridRowParams) => {
        console.log("Row clicked:", params.id);
    };

    return (
        <div style={{ height: 400, width: "100%" }}>
            <DataGrid
                rows={rows}
                columns={columns(onUpdateStatus)}
                checkboxSelection
                rowSelectionModel={selectionModel}
                onRowSelectionModelChange={handleSelection}
                onRowClick={handleRowClick}
                disableRowSelectionExcludeModel
            />
        </div>
    );
}
