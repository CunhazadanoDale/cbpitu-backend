import './Footer.css'

function Footer() {
    return (
        <footer className="footer">
            <div className="container">
                <div className="footer-content">
                    <div className="footer-brand">
                        <span className="logo-text">CB<span className="logo-highlight">Pitu</span></span>
                        <p>O campeonato que nasceu da amizade.</p>
                    </div>
                    <div className="footer-links">
                        <div className="footer-column">
                            <h4>Navegação</h4>
                            <a href="#sobre">Sobre</a>
                            <a href="#times">Times</a>
                            <a href="#edicoes">Edições</a>
                        </div>
                        <div className="footer-column">
                            <h4>Redes</h4>
                            <a href="#">Discord</a>
                            <a href="#">Instagram</a>
                            <a href="#">Twitter</a>
                        </div>
                    </div>
                </div>
                <div className="footer-bottom">
                    <p>&copy; 2024 CBPitu. Feito com 💜 entre amigos.</p>
                </div>
            </div>
        </footer>
    )
}

export default Footer
