import { BrowserRouter, Route, Routes } from 'react-router-dom'
import { BoardListPage } from './pages/BoardListPage'
import { BoardDetailPage } from './pages/BoardDetailPage'
import './App.css'

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<BoardListPage />} />
        <Route path="/boards/:boardId" element={<BoardDetailPage />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App
