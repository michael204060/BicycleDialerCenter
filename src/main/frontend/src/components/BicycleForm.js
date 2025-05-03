import React, { useState, useEffect } from 'react';
import { Modal, Button, Form, Alert } from 'react-bootstrap';
import { getAllUsers } from '../api';

const BicycleForm = ({ show, handleClose, handleSave, editingBicycle }) => {
    const [formData, setFormData] = useState({
        brand: '',
        model: '',
        type: '',
        price: '',
        assignedUserId: ''
    });
    const [users, setUsers] = useState([]);
    const [loadingUsers, setLoadingUsers] = useState(true);
    const [formError, setFormError] = useState(null);

    useEffect(() => {
        if (show) {
            fetchUsers();
        } else {
            setFormData({
                brand: '',
                model: '',
                type: '',
                price: '',
                assignedUserId: ''
            });
            setFormError(null);
        }
    }, [show]);

    useEffect(() => {
        if (editingBicycle) {
            const currentAssignedUser = editingBicycle.assignedUser;
            const currentAssignedUserId = currentAssignedUser ? users.find(user => user.email === currentAssignedUser.email || user.username === currentAssignedUser.username)?.id : '';

            setFormData({
                brand: editingBicycle.brand || '',
                model: editingBicycle.model || '',
                type: editingBicycle.type || '',
                price: editingBicycle.price != null ? parseFloat(editingBicycle.price).toFixed(2) : '',
                assignedUserId: currentAssignedUserId != null ? currentAssignedUserId : ''
            });
        } else {
            setFormData({
                brand: '',
                model: '',
                type: '',
                price: '',
                assignedUserId: ''
            });
        }
    }, [editingBicycle, users]);

    const fetchUsers = async () => {
        setLoadingUsers(true);
        try {
            const response = await getAllUsers();
            setUsers(response.data);
        } catch (error) {
            console.error('Error fetching users:', error);
            setFormError('Не удалось загрузить список пользователей для выбора назначенного пользователя.');
        } finally {
            setLoadingUsers(false);
        }
    };

    const handleAssignedUserChange = (e) => { // Изменено имя функции
        const assignedUserId = e.target.value === '' ? null : Number(e.target.value);
        setFormData({ ...formData, assignedUserId: assignedUserId });
    };


    const handleSubmit = () => {
        setFormError(null);

        if (!formData.brand || !formData.model || !formData.type || formData.price === '') {
            setFormError('Пожалуйста, заполните все обязательные поля (Бренд, Модель, Тип, Цена).');
            return;
        }

        const priceValue = parseFloat(formData.price);
        if (isNaN(priceValue) || priceValue < 0) {
            setFormError('Цена должна быть положительным числом.');
            return;
        }


        const dataToSend = {
            brand: formData.brand,
            model: formData.model,
            type: formData.type,
            price: priceValue,
            assignedUserId: formData.assignedUserId === '' ? null : formData.assignedUserId // Изменено имя поля
        };

        if (editingBicycle) {
            dataToSend.id = editingBicycle.id;
        }

        handleSave(dataToSend);
    };

    return (
        <Modal show={show} onHide={handleClose}>
            <Modal.Header closeButton>
                <Modal.Title>{editingBicycle ? 'Редактировать велосипед' : 'Добавить велосипед'}</Modal.Title>
            </Modal.Header>
            <Modal.Body>
                {formError && <Alert variant="danger">{formError}</Alert>}
                <Form>
                    <Form.Group className="mb-3">
                        <Form.Label>Бренд <span className="text-danger">*</span></Form.Label>
                        <Form.Control
                            type="text"
                            name="brand"
                            value={formData.brand}
                            onChange={handleChange}
                            required
                        />
                    </Form.Group>
                    <Form.Group className="mb-3">
                        <Form.Label>Модель <span className="text-danger">*</span></Form.Label>
                        <Form.Control
                            type="text"
                            name="model"
                            value={formData.model}
                            onChange={handleChange}
                            required
                        />
                    </Form.Group>
                    <Form.Group className="mb-3">
                        <Form.Label>Тип <span className="text-danger">*</span></Form.Label>
                        <Form.Control
                            type="text"
                            name="type"
                            value={formData.type}
                            onChange={handleChange}
                            required
                        />
                    </Form.Group>
                    <Form.Group className="mb-3">
                        <Form.Label>Цена <span className="text-danger">*</span></Form.Label>
                        <Form.Control
                            type="number"
                            name="price"
                            value={formData.price}
                            onChange={handleChange}
                            required
                            step="0.01"
                            min="0"
                        />
                    </Form.Group>
                    <Form.Group className="mb-3">
                        <Form.Label>Назначенный пользователь</Form.Label> {/* Изменен текст лейбла */}
                        {loadingUsers ? (
                            <Form.Control as="select" disabled>
                                <option value="">Загрузка пользователей...</option>
                            </Form.Control>
                        ) : (
                            <Form.Control
                                as="select"
                                name="assignedUserId" // Изменено имя поля
                                value={formData.assignedUserId || ''}
                                onChange={handleAssignedUserChange} // Изменено имя функции
                                disabled={users.length === 0}
                            >
                                <option value="">-- Нет назначенного пользователя --</option> {/* Изменен текст */}
                                {users.map(user => (
                                    <option key={user.id} value={user.id}>
                                        {user.username} ({user.email})
                                    </option>
                                ))}
                            </Form.Control>
                        )}
                    </Form.Group>
                </Form>
            </Modal.Body>
            <Modal.Footer>
                <Button variant="secondary" onClick={handleClose}>
                    Отмена
                </Button>
                <Button variant="primary" onClick={handleSubmit}>
                    Сохранить
                </Button>
            </Modal.Footer>
        </Modal>
    );
};

export default BicycleForm;