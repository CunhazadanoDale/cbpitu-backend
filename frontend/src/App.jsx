import { BrowserRouter, Routes, Route } from 'react-router-dom'
import Header from './components/Header/Header'
import Hero from './components/Hero/Hero'
import Sobre from './components/Sobre/Sobre'
import Times from './components/Times/Times'
import Edicoes from './components/Edicoes/Edicoes'
import CTA from './components/CTA/CTA'
import Footer from './components/Footer/Footer'
import Admin from './pages/Admin/Admin'
import Campeonato from './pages/Campeonato/Campeonato'
import EdicaoDetalhe from './pages/EdicaoDetalhe/EdicaoDetalhe'

function LandingPage() {
  return (
    <>
      <Header />
      <main>
        <Hero />
        <Sobre />
        <Times />
        <Edicoes />
        <CTA />
      </main>
      <Footer />
    </>
  )
}

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<LandingPage />} />
        <Route path="/admin" element={<Admin />} />
        <Route path="/campeonato/:id" element={<Campeonato />} />
        <Route path="/edicao/:id" element={<EdicaoDetalhe />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App
