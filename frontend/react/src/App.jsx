import { Button, ButtonGroup } from '@chakra-ui/react'
import SidebarWithHeader from "./shared/SideBar.jsx";

const App = () => {

  return (
      <SidebarWithHeader>
        <Button colorScheme='teal' variant='outline'>Click me</Button>
      </SidebarWithHeader>

  )
}

export default App
