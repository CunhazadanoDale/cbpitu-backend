import { useState, useEffect } from 'react'
import './Header.css'

function Header() {
    const [scrolled, setScrolled] = useState(false)
    const [menuOpen, setMenuOpen] = useState(false)

    useEffect(() => {
        const handleScroll = () => {
            setScrolled(window.scrollY > 50)
        }
        window.addEventListener('scroll', handleScroll)
        return () => window.removeEventListener('scroll', handleScroll)
    }, [])

    const handleNavClick = (e, targetId) => {
        e.preventDefault()
        setMenuOpen(false)
        const target = document.querySelector(targetId)
        if (target) {
            target.scrollIntoView({ behavior: 'smooth', block: 'start' })
        }
    }

    return (
        <header className={`header ${scrolled ? 'scrolled' : ''}`}>
            <nav className="navbar">
                <a href="#" className="logo">
                    <span className="logo-text">CB<span className="logo-highlight">Pitu</span></span>
                </a>
                <ul className={`nav-links ${menuOpen ? 'active' : ''}`}>
                    <li><a href="#sobre" onClick={(e) => handleNavClick(e, '#sobre')}>Sobre</a></li>
                    <li><a href="#times" onClick={(e) => handleNavClick(e, '#times')}>Times</a></li>
                    <li><a href="#edicoes" onClick={(e) => handleNavClick(e, '#edicoes')}>Edições</a></li>
                    <li><a href="#contato" className="btn-nav" onClick={(e) => handleNavClick(e, '#contato')}>Faça Parte</a></li>
                </ul>
                <button
                    className={`mobile-menu-btn ${menuOpen ? 'active' : ''}`}
                    aria-label="Menu"
                    onClick={() => setMenuOpen(!menuOpen)}
                >
                    <span></span>
                    <span></span>
                    <span></span>
                </button>
            </nav>
        </header>
    )
}

export default Header
