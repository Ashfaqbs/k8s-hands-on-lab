import axios from 'axios';
import { useEffect, useState } from 'react';

function EmployeeList() {
const [employees, setEmployees] = useState([]);

useEffect(() => {
fetchEmployees();
}, []);

const fetchEmployees = async () => {
try {
// const response = await axios.get('http://localhost:8080/api/users');
const response = await axios.get(`${import.meta.env.VITE_API_URL}`);
setEmployees(response.data);
} catch (error) {
console.error('Error fetching employees:', error);
}
};

const handleDelete = async (id) => {
try {
// await axios.delete(`http://localhost:8080/api/users/${id}`);
await axios.delete(`${import.meta.env.VITE_API_URL}/${id}`);
setEmployees(employees.filter(emp => emp.id !== id));
} catch (error) {
console.error('Error deleting employee:', error);
}
};

return (
<div>
<h2>Employee List</h2>
<a href="/add">Add New Employee</a>
<table>
<thead>
<tr>
<th>ID</th>
<th>Name</th>
<th>Email</th>
<th>Action</th>
</ tr>
</thead>
<tbody>
{employees.map(emp => (
<tr key={emp.id}>
<td>{emp.id}</td>
<td>{emp.name}</td>
<td>{emp.email}</td>
<td>
<button onClick={() => handleDelete(emp.id)}>Delete</button>
</td>
</tr>
))}
</tbody>
</table>
</div>
);
}

export default EmployeeList;