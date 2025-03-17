import axios from 'axios';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

function AddEmployee() {
const [formData, setFormData] = useState({ name: '', email: '' });
const navigate = useNavigate();

const handleChange = (e) => {
setFormData({ ...formData, [e.target.name]: e.target.value });
};

const handleSubmit = async (e) => {
e.preventDefault();
try {
// await axios.post('http://localhost:8080/api/users', formData);
await axios.post(`${import.meta.env.VITE_API_URL}`, formData);
navigate('/list'); // Redirect to list after success
} catch (error) {
console.error('Error adding employee:', error);
}
};

return (
<div>
<h2>Add Employee</h2>
<form onSubmit={handleSubmit}>
<input
type="text"
name="name"
placeholder="Name"
value={formData.name}
onChange={handleChange}
/>
<input
type="email"
name="email"
placeholder="Email"
value={formData.email}
onChange={handleChange}
/>
<button type="submit">Add</button>
</form>
</div>
);
}

export default AddEmployee;