import SidebarWithHeader from "./shared/SideBar.jsx";
import {Button} from "@chakra-ui/react";
import {useEffect} from "react";
import {getCustomers} from "./services/client.js";

const App = () => {

    useEffect(() => {
        getCustomers().then(res => {
            console.log(res)
        }).catch(err => {
            console.log(err)
        })
    }, []);

    return (
        <SidebarWithHeader>
            <Button colorScheme='teal' variant='outline'>CLICK ME!</Button>
        </SidebarWithHeader>
    )
}

export default App
