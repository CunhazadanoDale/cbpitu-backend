import './Edicoes.css'

const edicoes = [
    {
        ano: '2024',
        nome: 'CBPitu V',
        descricao: 'A maior edição de todas. 8 times, novo formato com fase de grupos + playoffs.',
        emAndamento: true,
        campeao: null
    },
    {
        ano: '2023',
        nome: 'CBPitu IV',
        descricao: '6 times disputaram o título. Final épica decidida no game 5.',
        emAndamento: false,
        campeao: 'Nome do Time Campeão'
    },
    {
        ano: '2022',
        nome: 'CBPitu III',
        descricao: 'Primeira edição com formato mata-mata. Surpresas e upsets marcaram o torneio.',
        emAndamento: false,
        campeao: 'Nome do Time Campeão'
    }
]

function EdicaoCard({ edicao }) {
    return (
        <div className={`edicao-card ${edicao.emAndamento ? 'current' : ''}`}>
            {edicao.emAndamento && <div className="edicao-badge">EM ANDAMENTO</div>}
            <h3>{edicao.nome}</h3>
            <p>{edicao.descricao}</p>
            {edicao.emAndamento ? (
                <div className="edicao-image">
                    <div className="image-placeholder large">
                        <span>BANNER EDIÇÃO 2024</span>
                    </div>
                </div>
            ) : (
                <div className="edicao-champion">
                    <span>🏆 Campeão:</span>
                    <strong>{edicao.campeao}</strong>
                </div>
            )}
        </div>
    )
}

function Edicoes() {
    return (
        <section id="edicoes" className="section edicoes">
            <div className="container">
                <div className="section-header">
                    <span className="section-tag">Histórico</span>
                    <h2 className="section-title">Conheça as <span className="gradient-text">Edições</span></h2>
                    <p className="section-description">A história do campeonato ao longo dos anos</p>
                </div>
                <div className="timeline">
                    {edicoes.map((edicao, index) => (
                        <div key={index} className="timeline-item">
                            <div className="timeline-marker">
                                <span className="timeline-year">{edicao.ano}</span>
                            </div>
                            <div className="timeline-content">
                                <EdicaoCard edicao={edicao} />
                            </div>
                        </div>
                    ))}
                </div>
            </div>
        </section>
    )
}

export default Edicoes
