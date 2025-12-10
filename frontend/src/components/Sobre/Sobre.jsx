import './Sobre.css'

const cards = [
    { icon: '🎮', title: 'Competição Real', desc: 'Partidas disputadas com seriedade, estratégia e muita emoção. Cada jogo importa.' },
    { icon: '🤝', title: 'Entre Amigos', desc: 'Criado por amigos, para amigos. A rivalidade fica no jogo, a amizade permanece.' },
    { icon: '🏆', title: 'Glória Eterna', desc: 'Quem vence, entra para a história. Seu nome será lembrado nas próximas edições.' },
]

function Sobre() {
    return (
        <section id="sobre" className="section sobre">
            <div className="container">
                <div className="section-header">
                    <span className="section-tag">Sobre Nós</span>
                    <h2 className="section-title">O Que é o <span className="gradient-text">CBPitu</span>?</h2>
                </div>
                <div className="sobre-grid">
                    {cards.map((card, index) => (
                        <div key={index} className="sobre-card">
                            <div className="sobre-icon">{card.icon}</div>
                            <h3>{card.title}</h3>
                            <p>{card.desc}</p>
                        </div>
                    ))}
                </div>
            </div>
        </section>
    )
}

export default Sobre
