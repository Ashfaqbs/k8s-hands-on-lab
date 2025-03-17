import { Link } from 'react-router-dom';

function Home() {
return (
<div>
<h2>Welcome to Employee Manager</h2>
<Link to="/list">Manage Employees</Link>
</div>
);
}

export default Home;