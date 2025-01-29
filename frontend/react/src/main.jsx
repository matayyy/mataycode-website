import {StrictMode} from 'react'
import {createRoot} from 'react-dom/client'
import './index.css'
import App from './App.jsx'
import {ChakraProvider} from "@chakra-ui/react";
import { createStandaloneToast } from '@chakra-ui/react'
import { createBrowserRouter, RouterProvider} from "react-router-dom";
import Login from "./components/login/Login.jsx";
import AuthProvider from "./components/context/AuthContext.jsx";
import ProtectedRoute from "./components/shared/ProtectedRoute.js";
import Signup from "./components/signup/Signup.jsx";

const { ToastContainer} = createStandaloneToast();

const router = createBrowserRouter([
    {
        path: "/",
        element: <Login />
    },
    {
        path:"dashboard",
        element: <ProtectedRoute><App /></ProtectedRoute>
    },
    {
        path:"signup",
        element: <Signup/>
    }
])


createRoot(document.getElementById('root')).render(
    <StrictMode>
        <ChakraProvider>
            <AuthProvider>
                <RouterProvider router={router} />
            </AuthProvider>
            <ToastContainer/>
        </ChakraProvider>
    </StrictMode>
)
