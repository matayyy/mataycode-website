import {
    Button,
    Drawer, DrawerBody,
    DrawerCloseButton,
    DrawerContent, DrawerFooter,
    DrawerHeader,
    DrawerOverlay,
    useDisclosure
} from "@chakra-ui/react";
import UpdateCustomerForm from "./UpdateCustomerForm.jsx";
import React from "react";

const AddIcon = () => "+"
const CloseIcon = () => "x"

const UpdateCustomerDrawer = ({fetchCustomers, initialValues, customerId}) => {

    const {isOpen, onOpen, onClose} = useDisclosure()

    return <>
        <Button bg={'gray.00'} color={'black'} rounded={'full'}
                _hover={{transform: 'translateY(-2px)', boxShadow: 'lg'}}
                onClick={onOpen}>
            Update customer
        </Button>

        <Drawer isOpen={isOpen} onClose={onClose} size={"xl"}>
            <DrawerOverlay/>
            <DrawerContent>
                <DrawerCloseButton/>
                <DrawerHeader>Update customer</DrawerHeader>

                <DrawerBody>
                    <UpdateCustomerForm
                        fetchCustomers={fetchCustomers}
                        initialValues={initialValues}
                        customerId={customerId}
                    />
                </DrawerBody>

                <DrawerFooter>
                    <Button leftIcon={<CloseIcon/>} onClick={onClose} colorScheme={"teal"}>
                        Close
                    </Button>
                </DrawerFooter>
            </DrawerContent>
        </Drawer>
    </>
}

export default UpdateCustomerDrawer;