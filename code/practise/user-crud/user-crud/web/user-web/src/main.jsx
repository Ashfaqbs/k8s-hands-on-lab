// import { StrictMode } from 'react'
// import { createRoot } from 'react-dom/client'
// import './index.css'
// import App from './App.jsx'

// createRoot(document.getElementById('root')).render(
//   <StrictMode>
//     <App />
//   </StrictMode>,
// )


import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter, Route, Routes } from 'react-router-dom';
import App from './App';
import AddEmployee from './pages/AddEmployee';
import EmployeeList from './pages/EmployeeList';
import Home from './pages/Home';

ReactDOM.createRoot(document.getElementById('root')).render(
<React.StrictMode>
<BrowserRouter>
<Routes>
<Route path="/" element={<App />}>
<Route index element={<Home />} />
<Route path="add" element={<AddEmployee />} />
<Route path="list" element={<EmployeeList />} />
</Route>
</Routes>
</BrowserRouter>
</React.StrictMode>
);