// import { useState } from 'react';
// import { Formik, Form, Field, ErrorMessage } from 'formik';
// import * as Yup from 'yup';
//
// function getValueByPath(obj, path) {
//     return path.split('.').reduce((acc, part) => acc?.[part] ?? '', obj);
// }
//
// function setValueByPath(obj, path, value) {
//     const keys = path.split('.');
//     const lastKey = keys.pop();
//     const newObj = { ...obj };
//     let current = newObj;
//
//     for (const key of keys) {
//         if (!current[key] || typeof current[key] !== 'object') {
//             current[key] = {};
//         }
//         current = current[key];
//     }
//
//     current[lastKey] = value;
//     return newObj;
// }
//
// export default function PostForm({ fields = [], defaultData = {}, onSubmit }) {
//     const [formValues, setFormValues] = useState(() => {
//         const initial = {};
//         fields.forEach(f => {
//             initial[f.key] = getValueByPath(defaultData, f.key);
//         });
//         return initial;
//     });
//
//     const handleChange = (e, key) => {
//         const value = e.target.value;
//         setFormValues(prev => ({
//             ...prev,
//             [key]: value
//         }));
//     };
//
//     const handleSubmit = (e) => {
//         e.preventDefault();
//         let structuredValues = {};
//         fields.forEach(f => {
//             structuredValues = setValueByPath(structuredValues, f.key, formValues[f.key]);
//         });
//
//         structuredValues.userId = structuredValues.userId || defaultData?.userId || 1;
//
//         onSubmit(structuredValues);
//     };
//
//     return (
//         <form onSubmit={handleSubmit} className="flex flex-col gap-4">
//             {fields.map(({ key, label, placeholder }) => (
//                 <div key={key}>
//                     <label className="block font-medium mb-1">{label}</label>
//                     <input
//                         type="text"
//                         value={formValues[key]}
//                         onChange={(e) => handleChange(e, key)}
//                         placeholder={placeholder}
//                         className="border p-2 rounded w-full"
//                     />
//                 </div>
//             ))}
//             <button type="submit" className="mt-4 bg-blue-600 text-white py-2 px-4 rounded hover:bg-blue-700">
//                 Сохранить
//             </button>
//         </form>
//     );
// }

import { Formik, Form, Field, ErrorMessage } from 'formik';
import * as Yup from 'yup';
import {useState} from "react";

function getValueByPath(obj, path) {
    return path.split('.').reduce((acc, part) => acc?.[part] ?? '', obj);
}

function setValueByPath(obj, path, value) {
    const keys = path.split('.');
    const lastKey = keys.pop();
    const newObj = { ...obj };
    let current = newObj;

    for (const key of keys) {
        if (!current[key] || typeof current[key] !== 'object') {
            current[key] = {};
        }
        current = current[key];
    }

    current[lastKey] = value;
    return newObj;
}

export default function PostForm({ fields = [], defaultData = {}, onSubmit }) {
    const [formValues, setFormValues] = useState(() => {
        const initial = {};
        fields.forEach(f => {
            initial[f.key] = getValueByPath(defaultData, f.key);
        });
        return initial;
    });

    const initialValues = {};
    const validationShape = {};

    fields.forEach(({ key, required, label }) => {
        initialValues[key] = getValueByPath(defaultData, key);
        validationShape[key] = required
            ? Yup.string().required(`Поле "${label}" обязательно`)
            : Yup.string();
    });

    const validationSchema = Yup.object(validationShape);

    const handleStructuredSubmit = (values) => {
        let structured = {};
        fields.forEach(f => {
            structured = setValueByPath(structured, f.key, values[f.key]);
        });

        structured.userId = structured.userId || defaultData?.userId || 1;

        onSubmit(structured);
    };

    return (
        <Formik
            initialValues={initialValues}
            validationSchema={validationSchema}
            onSubmit={handleStructuredSubmit}
        >
            <Form className="flex flex-col gap-4">
                {fields.map(({ key, label, placeholder }) => (
                    <div key={key}>
                        <label className="block font-medium mb-1">{label}</label>
                        <Field
                            name={key}
                            placeholder={placeholder}
                            className="border p-2 rounded w-full"
                        />
                        <ErrorMessage
                            name={key}
                            component="div"
                            className="text-red-600 text-sm mt-1"
                        />
                    </div>
                ))}
                <button
                    type="submit"
                    className="mt-4 bg-blue-600 text-white py-2 px-4 rounded hover:bg-blue-700"
                >
                    Сохранить
                </button>
            </Form>
        </Formik>
    );
}

