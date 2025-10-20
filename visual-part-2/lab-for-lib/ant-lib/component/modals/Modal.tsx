import React from "react";

interface ModalProps {
    isOpen: boolean;
    onClose: () => void;
    children: React.ReactNode;
}

export function Modal({ isOpen, onClose, children }: ModalProps) {
    if (!isOpen) return null;

    return (
        <div
            style={{
                position: "fixed",
                inset: 0,
                backgroundColor: "rgba(0,0,0,0.5)",
                display: "flex",
                justifyContent: "center",
                alignItems: "center",
                zIndex: 1000,
            }}
            onClick={onClose}
        >
            <div
                style={{ backgroundColor: "white", padding: 20, borderRadius: 5, minWidth: 300 }}
                onClick={(e) => e.stopPropagation()}
            >
                <button onClick={onClose} style={{ float: "right" }}>
                    X
                </button>
                {children}
            </div>
        </div>
    );
}
