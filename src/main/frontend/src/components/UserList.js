import React, { useState, useEffect } from 'react';
import { Table, Button, Container, Row, Col, Alert, Spinner } from 'react-bootstrap';
import { getAllUsers, createUser, updateUser, deleteUser } from '../api';
import UserForm from './UserForm';

const UserList = () => {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [showModal, setShowModal] = useState(false);
    const [editingUser, setEditingUser] = useState(null);

    useEffect(() => {
        fetchUsers();
    }, []);

    const fetchUsers = async () => {
        setLoading(true);
        setError(null);
        try {
            const response = await getAllUsers();
            setUsers(response.data);
        } catch (err) {
            console.error('Fetch users error:', err.response || err.message);
            setError('Не удалось загрузить список пользователей. ' + (err.response?.data?.message || err.message));
        } finally {
            setLoading(false);
        }
    };

    const handleShowModal = (user = null) => {
        setEditingUser(user);
        setShowModal(true);
    };

    const handleCloseModal = () => {
        setShowModal(false);
        setEditingUser(null);
    };

    const handleSaveUser = async (formData) => {
        setError(null);

        try {
            if (formData.id) {
                await updateUser(formData.id, formData);
            } else {
                await createUser(formData);
            }
            fetchUsers();
            handleCloseModal();
        } catch (err) {
            console.error('Save user error:', err.response || err.message);
            const errorMessage = err.response?.data?.message || err.message || 'Неизвестная ошибка при сохранении.';
            alert('Ошибка сохранения: ' + errorMessage);
        }
    };

    const handleDeleteUser = async (id) => {
        if (window.confirm('Вы уверены, что хотите удалить этого пользователя? При удалении пользователя, все велосипеды, владельцем которых он является, потеряют владельца.')) {
            setError(null);
            try {
                await deleteUser(id);
                fetchUsers();
            } catch (err) {
                console.error('Delete user error:', err.response || err.message);
                const errorMessage = err.response?.data?.message || err.message || 'Неизвестная ошибка при удалении.';
                setError('Ошибка при удалении пользователя: ' + errorMessage);
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

    if (error && users.length === 0) {
        return <Container className="mt-4"><Alert variant="danger">{error}</Alert></Container>;
    }

    return (
        <Container className="mt-4">
            <Row className="mb-3 align-items-center">
                <Col>
                    <h2>Список Пользователей</h2>
                </Col>
                <Col className="text-end">
                    <Button variant="primary" onClick={() => handleShowModal()}>
                        Добавить Пользователя
                    </Button>
                </Col>
            </Row>

            {error && users.length > 0 && <Alert variant="danger">{error}</Alert>}

            <Table striped bordered hover responsive>
                <thead>
                <tr>
                    <th>Логин</th>
                    <th>Email</th>
                    <th>Действия</th>
                </tr>
                </thead>
                <tbody>
                {users.length === 0 && !loading && !error ? (
                    <tr>
                        <td colSpan={3} className="text-center">Нет данных о пользователях.</td>
                    </tr>
                ) : (
                    users.map((user) => (
                        <tr key={user.id}>
                            <td>{user.username}</td>
                            <td>{user.email}</td>
                            <td>
                                <Button
                                    variant="outline-primary"
                                    size="sm"
                                    className="me-2"
                                    onClick={() => handleShowModal(user)}
                                >
                                    Редактировать
                                </Button>
                                <Button
                                    variant="outline-danger"
                                    size="sm"
                                    onClick={() => handleDeleteUser(user.id)}
                                >
                                    Удалить
                                </Button>
                            </td>
                        </tr>
                    ))
                )}
                </tbody>
            </Table>

            <UserForm
                show={showModal}
                handleClose={handleCloseModal}
                handleSave={handleSaveUser}
                editingUser={editingUser}
            />
        </Container>
    );
};

export default UserList;