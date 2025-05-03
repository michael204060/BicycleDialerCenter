import React, { useState, useEffect } from 'react';
import { Modal, Button, Form, Alert } from 'react-bootstrap';

const UserForm = ({ show, handleClose, handleSave, editingUser }) => {
    const [formData, setFormData] = useState({
        username: '',
        email: '',
        password: ''
    });
    const [formError, setFormError] = useState(null);

    useEffect(() => {
        if (editingUser) {
            setFormData({
                username: editingUser.username || '',
                email: editingUser.email || '',
                password: ''
            });
        } else {
            setFormData({
                username: '',
                email: '',
                password: ''
            });
        }
    }, [editingUser]);

    useEffect(() => {
        if (!show) {
            setFormError(null);
        }
    }, [show]);


    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData({ ...formData, [name]: value });
    };

    const handleSubmit = () => {
        setFormError(null);

        if (!formData.username || !formData.email) {
            setFormError('Пожалуйста, заполните все обязательные поля (Логин, Email).');
            return;
        }
        if (!editingUser && !formData.password) {
            setFormError('Пароль обязателен для нового пользователя.');
            return;
        }


        const dataToSend = {
            username: formData.username,
            email: formData.email,
            ...(formData.password && { password: formData.password })
        };

        if (editingUser) {
            dataToSend.id = editingUser.id;
        }

        handleSave(dataToSend);
    };

    return (
        <Modal show={show} onHide={handleClose}>
            <Modal.Header closeButton>
                <Modal.Title>{editingUser ? 'Редактировать пользователя' : 'Добавить пользователя'}</Modal.Title>
            </Modal.Header>
            <Modal.Body>
                {formError && <Alert variant="danger">{formError}</Alert>}
                <Form>
                    <Form.Group className="mb-3">
                        <Form.Label>Логин <span className="text-danger">*</span></Form.Label>
                        <Form.Control
                            type="text"
                            name="username"
                            value={formData.username}
                            onChange={handleChange}
                            required
                        />
                    </Form.Group>
                    <Form.Group className="mb-3">
                        <Form.Label>Email <span className="text-danger">*</span></Form.Label>
                        <Form.Control
                            type="email"
                            name="email"
                            value={formData.email}
                            onChange={handleChange}
                            required
                        />
                    </Form.Group>
                    {!editingUser && (
                        <Form.Group className="mb-3">
                            <Form.Label>Пароль <span className="text-danger">*</span></Form.Label>
                            <Form.Control
                                type="password"
                                name="password"
                                value={formData.password}
                                onChange={handleChange}
                                required={!editingUser}
                            />
                        </Form.Group>
                    )}
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

export default UserForm;