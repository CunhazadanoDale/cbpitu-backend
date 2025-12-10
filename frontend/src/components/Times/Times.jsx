import './Times.css'

// Dados mockados - depois virão da API
const times = [
    { id: 1, nome: 'Nome do Time 1', titulo: '🏆 2x Campeão', jogadores: 5 },
    { id: 2, nome: 'Nome do Time 2', titulo: '🏆 1x Campeão', jogadores: 5 },
    { id: 3, nome: 'Nome do Time 3', titulo: 'Vice-campeão 2023', jogadores: 5 },
    { id: 4, nome: 'Nome do Time 4', titulo: 'Novato 2024', jogadores: 5 },
]

function TimeCard({ time }) {
    return (
        <div className="time-card">
            <div className="time-card-image">
                <div className="image-placeholder">
                    <span>LOGO</span>
                </div>
            </div>
            <div className="time-card-content">
                <h3 className="time-name">{time.nome}</h3>
                <p className="time-titles">{time.titulo}</p>
                <div className="time-players">
                    <span>{time.jogadores} jogadores</span>
                </div>
            </div>
        </div>
    )
}

function Times() {
    return (
        <section id="times" className="section times">
            <div className="container">
                <div className="section-header">
                    <span className="section-tag">Participantes</span>
                    <h2 className="section-title">Conheça os <span className="gradient-text">Times</span></h2>
                    <p className="section-description">Os esquadrões que disputam a glória do CBPitu</p>
                </div>
                <div className="times-grid">
                    {times.map(time => (
                        <TimeCard key={time.id} time={time} />
                    ))}
                </div>
                <div className="times-cta">
                    <a href="#" className="btn btn-outline">Ver Todos os Times</a>
                </div>
            </div>
        </section>
    )
}

export default Times
