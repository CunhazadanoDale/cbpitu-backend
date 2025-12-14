import { useEffect, useRef } from 'react'
import './Hero.css'

function Hero() {
    const contentRef = useRef(null)

    useEffect(() => {
        const handleScroll = () => {
            if (contentRef.current) {
                const scrolled = window.pageYOffset
                if (scrolled < window.innerHeight) {
                    contentRef.current.style.transform = `translateY(${scrolled * 0.3}px)`
                    contentRef.current.style.opacity = 1 - (scrolled / window.innerHeight)
                }
            }
        }
        window.addEventListener('scroll', handleScroll)
        return () => window.removeEventListener('scroll', handleScroll)
    }, [])

    return (
        <section className="hero">
            <div className="hero-bg-overlay"></div>
            <div className="hero-content" ref={contentRef}>
                <div className="hero-badge">🏆 Edição 2025</div>
                <h1 className="hero-title">
                    O Maior Campeonato<br />
                    <span className="gradient-text">Entre Amigos</span>
                </h1>
                <p className="hero-subtitle">
                    Rivalidade saudável, partidas épicas e muita diversão.
                    Bem-vindo ao CBPitu, onde lendas são forjadas.
                </p>
                <div className="hero-cta">
                    <a href="#times" className="btn btn-primary">Ver Times</a>
                    <a href="#edicoes" className="btn btn-secondary">Histórico</a>
                </div>
                <div className="hero-stats">
                    <div className="stat">
                        <span className="stat-number">8</span>
                        <span className="stat-label">Times</span>
                    </div>
                    <div className="stat">
                        <span className="stat-number">5</span>
                        <span className="stat-label">Edições</span>
                    </div>
                    <div className="stat">
                        <span className="stat-number">40+</span>
                        <span className="stat-label">Jogadores</span>
                    </div>
                </div>
            </div>
            <div className="hero-scroll-indicator">
                <span>Scroll</span>
                <div className="scroll-line"></div>
            </div>
        </section>
    )
}

export default Hero
