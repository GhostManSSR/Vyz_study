"use client";

import React, { useState } from "react";
import { Form, Input, Select, DatePicker, message, Card, Row, Col, Statistic } from "antd";
import { Line, Pie, Column } from "@ant-design/charts";
import type { Moment } from "moment";

interface ProjectFormValues {
    names: string;
    description: string;
    priority: "priority_asc" | "priority_desc";
    category: string[];
    date: Moment;
    budget: number;
}

type LineData = { date: string; budget: number };
type PieData = { type: string; value: number };
type ColumnData = { category: string; count: number };

const DefaultForm: React.FC = () => {
    const [form] = Form.useForm<ProjectFormValues>();
    const [projects, setProjects] = useState<ProjectFormValues[]>([]);

    const onFinish = (values: ProjectFormValues) => {
        setProjects((prev) => [...prev, values]);
        console.log("onFinish:", values);
        message.success("Проект создан");
        form.resetFields();
    };

    const lineData: LineData[] = projects.map((p) => ({
        date: p.date.format("YYYY-MM-DD"),
        budget: p.budget,
    }));

    const priorityCount = projects.reduce<Record<string, number>>((acc, p) => {
        if (p.priority) {
            acc[p.priority] = (acc[p.priority] || 0) + 1;
        }
        return acc;
    }, {});

    const categoryCount: Record<string, number> = {};
    projects.forEach((p) => {
        p.category.forEach((cat) => {
            categoryCount[cat] = (categoryCount[cat] || 0) + 1;
        });
    });
    const columnData: ColumnData[] = Object.entries(categoryCount).map(([category, count]) => ({ category, count }));

    const lineConfig = {
        data: lineData,
        xField: "date",
        yField: "budget",
        smooth: true,
        height: 300,
    };

    const pieData = Object.entries(priorityCount).map(([name, value]) => ({ name, value }));


    console.log(pieData)
    const testData = [
        { name: "priority_asc", value: 1 },
        { name: "priority_desc", value: 1 },
    ];

    const pieConfig = {
        data: pieData,
        angleField: 'value',
        colorField: 'name',
        label: {
            text: 'value',
            style: {
                fontWeight: 'bold',
            },
        },
        legend: {
            color: {
                title: false,
                position: 'right',
                rowPadding: 5,
            },
        },
    };

    const columnConfig = {
        data: columnData,
        xField: "category",
        yField: "count",
        height: 300,
    };

    const totalProjects = projects.length;
    const totalBudget = projects.reduce((acc, p) => acc + Number(p.budget), 0);

    const categories = Object.keys(categoryCount).length;

    return (
        <div style={{ padding: 24 }}>
            <Row gutter={[16, 16]}>
                <Col xs={24} md={12} lg={8}>
                    <Card>
                        <Form form={form} layout="vertical" onFinish={onFinish}>
                            <Form.Item
                                label="Название проекта"
                                name="names"
                                rules={[{ required: true, message: "Введите название проекта" }]}
                            >
                                <Input />
                            </Form.Item>
                            <Form.Item
                                label="Описание проекта"
                                name="description"
                                rules={[{ required: true, message: "Введите описание проекта" }]}
                            >
                                <Input.TextArea rows={4} />
                            </Form.Item>
                            <Form.Item
                                label="Приоритет"
                                name="priority"
                                rules={[{ required: true, message: "Выберите приоритет" }]}
                            >
                                <Select
                                    options={[
                                        { label: "Высокий", value: "Высокий" },
                                        { label: "Низкий", value: "Низкий" },
                                    ]}
                                    placeholder="Выберите приоритет"
                                />
                            </Form.Item>
                            <Form.Item
                                label="Категории"
                                name="category"
                                rules={[{ required: true, message: "Выберите категории" }]}
                            >
                                <Select
                                    mode="multiple"
                                    options={[
                                        { label: "Категория 1", value: "cat1" },
                                        { label: "Категория 2", value: "cat2" },
                                        { label: "Категория 3", value: "cat3" },
                                    ]}
                                    placeholder="Выберите категории"
                                />
                            </Form.Item>
                            <Form.Item
                                label="Дедлайн"
                                name="date"
                                rules={[{ required: true, message: "Выберите дату дедлайна" }]}
                            >
                                <DatePicker style={{ width: "100%" }} />
                            </Form.Item>
                            <Form.Item
                                label="Бюджет"
                                name="budget"
                                rules={[{ required: true, message: "Введите бюджет" }]}
                            >
                                <Input
                                    type="number"
                                    min={0}
                                    onKeyDown={(e) => {
                                        if (e.key === "-" || e.key === "e" || e.key === "+") {
                                            e.preventDefault();
                                        }
                                    }}
                                />
                            </Form.Item>
                            <Form.Item>
                                <button type="submit" style={{ marginRight: 8 }}>
                                    Отправить
                                </button>
                                <button type="button" onClick={() => form.resetFields()}>
                                    Очистить
                                </button>
                            </Form.Item>
                        </Form>
                    </Card>
                </Col>

                {/* Дашборд с графиками и статистикой */}
                <Col xs={24} md={12} lg={16}>
                    <Row gutter={[16, 16]}>
                        <Col xs={24} sm={8}>
                            <Card>
                                <Statistic title="Всего проектов" value={totalProjects} />
                            </Card>
                        </Col>
                        <Col xs={24} sm={8}>
                            <Card>
                                <Statistic
                                    title="Общий бюджет"
                                    value={totalBudget}
                                    precision={2}
                                    prefix="$"
                                    formatter={(value) =>
                                        new Intl.NumberFormat('ru-RU', { minimumFractionDigits: 2 }).format(Number(value))
                                    }
                                />
                            </Card>
                        </Col>
                        <Col xs={24} sm={8}>
                            <Card>
                                <Statistic title="Категорий" value={categories} />
                            </Card>
                        </Col>
                    </Row>

                    <Row gutter={[16, 16]} style={{ marginTop: 24 }}>
                        <Col xs={24} lg={12}>
                            <Card title="Тренд бюджета">
                                <Line {...lineConfig} />
                            </Card>
                        </Col>
                        <Col xs={24} lg={12}>
                            <Card title="Распределение по приоритету">
                                    <Pie {...pieConfig} />
                            </Card>
                        </Col>
                    </Row>

                    <Row gutter={[16, 16]} style={{ marginTop: 24 }}>
                        <Col xs={24}>
                            <Card title="Количество проектов по категориям">
                                <Column {...columnConfig} />
                            </Card>
                        </Col>
                    </Row>
                </Col>
            </Row>
        </div>
    );
};

export default DefaultForm;
