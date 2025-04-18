import { NavLink } from 'react-router-dom';

const SideBar = ({...props}) => {


    return (
        <div className="w-48 h-screen bg-gray-100 p-4 fixed" style={{display: 'flex', alignItems: 'center'}}>
            <nav className="flex flex-col gap-2" style={{display: 'flex', alignItems: 'center', justifyContent: 'space-between', width: '100%'}}>
                <NavLink to="/posts" className="hover:underline" style={{display:"flex", justifyContent:"center", alignContent:"center", borderRadius:"20px", padding:"1em 2em",background:"aqua", textDecoration:"none"}}>Posts</NavLink>
                <NavLink to="/albums" className="hover:underline">Albums</NavLink>
                <NavLink to="/todos" className="hover:underline">Todos</NavLink>
                <NavLink to="/users" className="hover:underline">Users</NavLink>
            </nav>
        </div>
    );
}

export default SideBar;