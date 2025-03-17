import { Outlet } from 'react-router-dom';

function App() {
return (
<div>
<h1>Employee Manager</h1>
<Outlet /> {/* Renders child routes */}
</div>
);
}

export default App;