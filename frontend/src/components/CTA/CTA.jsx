import './CTA.css'

function CTA() {
    return (
        <section id="contato" className="section cta-section">
            <div className="container">
                <div className="cta-content">
                    <h2 className="cta-title">Quer <span className="gradient-text">Fazer Parte</span>?</h2>
                    <p className="cta-description">
                        Monte seu time, reúna seus amigos e venha disputar a próxima edição do CBPitu.
                        A glória te espera!
                    </p>
                    <div className="cta-buttons">
                        <a href="#" className="btn btn-primary btn-large">Inscrever Time</a>
                        <a href="#" className="btn btn-ghost">Falar Conosco</a>
                    </div>
                </div>
                <div className="cta-image">
                    <div className="image-placeholder xlarge">
                        <span>IMAGEM DECORATIVA</span>
                    </div>
                </div>
            </div>
        </section>
    )
}

export default CTA
