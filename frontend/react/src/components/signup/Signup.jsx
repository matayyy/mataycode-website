import {useNavigate} from "react-router-dom";
import {useAuth} from "../context/AuthContext.jsx";
import {Flex, Heading, Image, Link, Stack, Text} from "@chakra-ui/react";
import logo from "../../assets/jellyfish-icon.png";
import CreateCustomerForm from "../shared/CreateCustomerForm.jsx";
import {useEffect} from "react";

const Signup = () => {
    const {customer, setCustomerFromToken} = useAuth();
    const navigate = useNavigate()

    useEffect(() => {
        if (customer) {
            navigate("/dashboard/customers")
        }
    });

    return (
        <Stack minH={'100vh'} direction={{base: 'column', md: 'row'}}>
            <Flex p={8} flex={1} alignItems={'center'} justify={'center'}>
                <Stack spacing={4} w={'full'} maxW={'md'}>
                    <Image src={logo} boxSize={"200px"} alt={"Mataycode Logo"}/>
                    <Heading fontSize={'2xl'} mb={15}>Register your account</Heading>
                    <CreateCustomerForm onSuccess={(token) => {
                        localStorage.setItem("access_token", token)
                        setCustomerFromToken()
                        navigate("/dashboard")
                    }}/>
                    <Link color={"blue.500"} href={"/"}>Have an account? Signup now.</Link>
                </Stack>
            </Flex>
            <Flex flex={1} padding={10} flexDirection={"column"} alignItems={"center"} justifyContent={"center"}
                  bgGradient={{sm: 'linear(to-r, blue.600, purple.600)'}}>
                <Text fontSize={"6xl"} color={'white'} fontWeight={"bold"} mb={5}>
                    <Link href={"https://github.com/matayyy/mataycode-website"}>
                        Check code at github!
                    </Link>
                </Text>
                <Image
                    alt={'Login Image'}
                    objectFit={'scale-down'}
                    src={
                        'https://user-images.githubusercontent.com/40702606/215539167-d7006790-b880-4929-83fb-c43fa74f429e.png'
                    }
                />
            </Flex>
        </Stack>
    )

}
export default Signup;