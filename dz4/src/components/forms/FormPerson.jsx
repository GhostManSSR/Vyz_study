import { Formik, Form, Field, ErrorMessage } from 'formik';
import * as Yup from 'yup';
import { useState } from "react";
import Button from "../Button";

// Используем Yup-схему валидации:
const validationSchema = Yup.object({
    name: Yup.string()
        .max(15, 'Must be 15 characters or less')
        .required('Required'),
    age: Yup.number()
        .typeError('Must be a number')
        .positive('Must be positive')
        .required('Required'),
    email: Yup.string()
        .email('Invalid email address')
        .required('Required'),
    petsName: Yup.string().when('pets', {
        then: Yup.string().required('Required')
    }),
    petsAge: Yup.number().when('pets', {
        then: Yup.number()
            .typeError('Must be a number')
            .required('Required')
    })
});

const FormPerson = ({ person }) => {
    const [pets, setPets] = useState(false);

    return (
        <div style={{ display: "flex" }}>
            <Formik
                initialValues={{ name: '', age: '', email: '', petsName: '', petsAge: '', pets }}
                validationSchema={validationSchema}
                onSubmit={(values, { setSubmitting }) => {
                    const newPerson = {
                        name: values.name,
                        age: values.age,
                        email: values.email,
                        pets: pets ? [{ name: values.petsName, age: values.petsAge }] : []
                    };
                    person.push(newPerson);
                    console.log(newPerson);
                    setSubmitting(false);
                }}
            >
                {({ isSubmitting, setFieldValue }) => (
                    <Form>
                        <div style={{ display: "flex", gap: "10px", flexDirection: "column", marginLeft: "250px", marginRight: "auto" }}>
                            <span>Name</span>
                            <Field type="text" name="name" />
                            <ErrorMessage name="name" component="div" />

                            <span>Age</span>
                            <Field type="number" name="age" />
                            <ErrorMessage name="age" component="div" />

                            <span>Email</span>
                            <Field type="email" name="email" />
                            <ErrorMessage name="email" component="div" />

                            <Button onClick={() => {
                                setPets(true);
                                // setFieldValue("pets", true);
                            }}>Добавить питомца</Button>

                            {pets && (
                                <div style={{ display: "flex", justifyContent: "space-between", flexDirection: "column", gap: "10px" }}>
                                    <span>Pets</span>
                                    <span>Name</span>
                                    <Field type="text" name="petsName" />
                                    <ErrorMessage name="petsName" component="div" />
                                    <span>Age</span>
                                    <Field type="number" name="petsAge" />
                                    <ErrorMessage name="petsAge" component="div" />
                                </div>
                            )}

                            <button type="submit" disabled={isSubmitting}>
                                Submit
                            </button>
                        </div>
                    </Form>
                )}
            </Formik>
        </div>
    );
}

export default FormPerson;
