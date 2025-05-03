import React, { useState, useEffect } from 'react';
import { Modal, Button, Form, Alert, Spinner } from 'react-bootstrap';
import { getAllUsers, getAllBicycles, rentBicycle } from '../api';

const RentalForm = ({ show, handleClose, onRentalCreated }) => {
    const [users, setUsers] = useState([]);
    const [bicycles, setBicycles] = useState([]);
    const [loading, setLoading] = useState(true);
    const [selectedUserId, setSelectedUserId] = useState('');
    const [selectedBicycleId, setSelectedBicycleId] = useState('');
    const [formError, setFormError] = useState(null);
    const [isSubmitting, setIsSubmitting] = useState(false);

    useEffect(() => {
        if (show) {
            fetchData();
        } else {
            setSelectedUserId('');
            setSelectedBicycleId('');
            setFormError(null);
            setIsSubmitting(false);
        }
    }, [show]);

    const fetchData = async () => {
        setLoading(true);
        setFormError(null);
        try {
            const [usersResponse, bicyclesResponse] = await Promise.all([
                getAllUsers(),
                getAllBicycles()
            ]);
            setUsers(usersResponse.data);
            setBicycles(bicyclesResponse.data);
        } catch (err) {
            console.error('Error fetching data for rental form:', err);
            setFormError('Не удалось загрузить пользователей или велосипеды.');
        } finally {
            setLoading(false);
        }
    };

    const handleSubmit = async () => {
        setFormError(null);
        if (!selectedUserId || !selectedBicycleId) {
            setFormError('Пожалуйста, выберите пользователя и велосипед.');
            return;
        }
        setIsSubmitting(true);
        try {
            const response = await rentBicycle(Number(selectedUserId), Number(selectedBicycleId));
            onRentalCreated(response.data);
            handleClose();
        } catch (err) {
            console.error('Error creating rental:', err.response || err.message);
            const errorMessage = err.response?.data?.message || err.message || 'Неизвестная ошибка при аренде.';
            setFormError('Ошибка аренды: ' + errorMessage);
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <Modal show={show} onHide={handleClose}>
            <Modal.Header closeButton>
                <Modal.Title>Арендовать Велосипед</Modal.Title>
            </Modal.Header>
            <Modal.Body>
                {formError && <Alert variant="danger">{formError}</Alert>}
                {loading ? (
                    <div className="text-center">
                        <Spinner animation="border" size="sm" /> Загрузка...
                    </div>
                ) : (
                    <Form>
                        <Form.Group className="mb-3">
                            <Form.Label>Пользователь</Form.Label>
                            <Form.Control
                                as="select"
                                value={selectedUserId}
                                onChange={(e) => setSelectedUserId(e.target.value)}
                                required
                            >
                                <option value="">-- Выберите пользователя --</option>
                                {users.map(user => (
                                    <option key={user.id} value={user.id}>{user.username} ({user.email})</option>
                                ))}
                            </Form.Control>
                        </Form.Group>
                        <Form.Group className="mb-3">
                            <Form.Label>Велосипед</Form.Label>
                            <Form.Control
                                as="select"
                                value={selectedBicycleId}
                                onChange={(e) => setSelectedBicycleId(e.target.value)}
                                required
                            >
                                <option value="">-- Выберите велосипед --</option>
                                {bicycles.map(bicycle => (
                                    <option key={bicycle.id} value={bicycle.id}>{bicycle.brand} {bicycle.model} ({bicycle.assignedUser ? 'Назначенный пользователь: ' + bicycle.assignedUser.username : 'Без назначенного пользователя'})</option>
                                ))}
                            </Form.Control>
                        </Form.Group>
                    </Form>
                )}
            </Modal.Body>
            <Modal.Footer>
                <Button variant="secondary" onClick={handleClose} disabled={isSubmitting}>
                    Отмена
                </Button>
                <Button variant="primary" onClick={handleSubmit} disabled={isSubmitting || loading || !selectedUserId || !selectedBicycleId}>
                    {isSubmitting ? <Spinner animation="border" size="sm" /> : 'Арендовать'}
                </Button>
            </Modal.Footer>
        </Modal>
    );
};

export default RentalForm;