import React, { useState, useEffect } from 'react';
import { Table, Button, Container, Row, Col, Alert, Spinner } from 'react-bootstrap';
import { getAllBicycles, createBicycle, updateBicycle, deleteBicycle } from '../api';
import BicycleForm from './BicycleForm';

const BicycleList = () => {
    const [bicycles, setBicycles] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [showModal, setShowModal] = useState(false);
    const [editingBicycle, setEditingBicycle] = useState(null);

    useEffect(() => {
        fetchBicycles();
    }, []);

    const fetchBicycles = async () => {
        setLoading(true);
        setError(null);
        try {
            const response = await getAllBicycles();
            setBicycles(response.data.map(bike => ({
                ...bike,
                price: bike.price != null ? parseFloat(bike.price).toFixed(2) : 'N/A',
            })));
        } catch (err) {
            console.error('Fetch bicycles error:', err.response || err.message);
            setError('Не удалось загрузить список велосипедов. ' + (err.response?.data?.message || err.message));
        } finally {
            setLoading(false);
        }
    };

    const handleShowModal = (bicycle = null) => {
        setEditingBicycle(bicycle);
        setShowModal(true);
    };

    const handleCloseModal = () => {
        setShowModal(false);
        setEditingBicycle(null);
    };

    const handleSaveBicycle = async (formData) => {
        setError(null);

        try {
            if (formData.id) {
                await updateBicycle(formData.id, formData);
            } else {
                await createBicycle(formData);
            }
            fetchBicycles();
            handleCloseModal();
        } catch (err) {
            console.error('Save bicycle error:', err.response || err.message);
            const errorMessage = err.response?.data?.message || err.message || 'Неизвестная ошибка при сохранении.';
            alert('Ошибка сохранения: ' + errorMessage);
        }
    };

    const handleDeleteBicycle = async (id) => {
        if (window.confirm('Вы уверены, что хотите удалить этот велосипед?')) {
            setError(null);
            try {
                await deleteBicycle(id);
                fetchBicycles();
            } catch (err) {
                console.error('Delete bicycle error:', err.response || err.message);
                const errorMessage = err.response?.data?.message || err.message || 'Неизвестная ошибка при удалении.';
                setError('Ошибка при удалении велосипеда: ' + errorMessage);
            }
        }
    };

    if (loading) {
        return (
            <Container className="mt-4 text-center">
                <Spinner animation="border" role="status">
                    <span className="visually-hidden">Загрузка...</span>
                </Spinner>
                <p>Загрузка данных...</p>
            </Container>
        );
    }

    if (error && bicycles.length === 0) {
        return <Container className="mt-4"><Alert variant="danger">{error}</Alert></Container>;
    }


    return (
        <Container className="mt-4">
            <Row className="mb-3 align-items-center">
                <Col>
                    <h2>Список Велосипедов</h2>
                </Col>
                <Col className="text-end">
                    <Button variant="primary" onClick={() => handleShowModal()}>
                        Добавить Велосипед
                    </Button>
                </Col>
            </Row>

            {error && bicycles.length > 0 && <Alert variant="danger">{error}</Alert>}

            <Table striped bordered hover responsive>
                <thead>
                <tr>
                    <th>Бренд</th>
                    <th>Модель</th>
                    <th>Тип</th>
                    <th>Цена</th>
                    <th>Назначенный пользователь (ManyToOne)</th> {/* Изменен текст */}
                    <th>Действия</th>
                </tr>
                </thead>
                <tbody>
                {bicycles.length === 0 && !loading && !error ? (
                    <tr>
                        <td colSpan={6} className="text-center">Нет данных о велосипедах.</td>
                    </tr>
                ) : (
                    bicycles.map((bicycle) => (
                        <tr key={bicycle.id}>
                            <td>{bicycle.brand}</td>
                            <td>{bicycle.model}</td>
                            <td>{bicycle.type}</td>
                            <td>{bicycle.price}</td>
                            <td>
                                {bicycle.assignedUser ? // Изменено имя поля
                                    `${bicycle.assignedUser.username || 'N/A'} (${bicycle.assignedUser.email || 'N/A'})` // Изменено имя поля
                                    : 'Нет назначенного пользователя'} {/* Изменен текст */}
                            </td>
                            <td>
                                <Button
                                    variant="outline-primary"
                                    size="sm"
                                    className="me-2"
                                    onClick={() => handleShowModal(bicycle)}
                                >
                                    Редактировать
                                </Button>
                                <Button
                                    variant="outline-danger"
                                    size="sm"
                                    onClick={() => handleDeleteBicycle(bicycle.id)}
                                >
                                    Удалить
                                </Button>
                            </td>
                        </tr>
                    ))
                )}
                </tbody>
            </Table>

            <BicycleForm
                show={showModal}
                handleClose={handleCloseModal}
                handleSave={handleSaveBicycle}
                editingBicycle={editingBicycle}
            />
        </Container>
    );
};

export default BicycleList;