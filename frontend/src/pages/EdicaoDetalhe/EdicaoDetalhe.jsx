import { useState, useEffect } from 'react'
import { useParams, Link } from 'react-router-dom'
import { edicoesApi, campeonatosApi } from '../../services/api'
import './EdicaoDetalhe.css'

function EdicaoDetalhe() {
    const { id } = useParams()
    const [edicao, setEdicao] = useState(null)
    const [escalacoes, setEscalacoes] = useState([])
    const [campeonatos, setCampeonatos] = useState([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState(null)
    const [timeExpandido, setTimeExpandido] = useState(null)

    useEffect(() => {
        carregarDados()
    }, [id])

    const carregarDados = async () => {
        setLoading(true)
        setError(null)
        try {
            // Carrega dados da edição
            const edicaoData = await edicoesApi.buscarPorId(id)
            setEdicao(edicaoData)

            // Carrega escalações (times participantes)
            try {
                const escalacoesData = await edicoesApi.buscarEscalacoes(id)
                setEscalacoes(escalacoesData || [])
            } catch {
                setEscalacoes([])
            }

            // Carrega campeonatos e filtra por edição
            try {
                const todosCampeonatos = await campeonatosApi.listar()
                const campeonatosDaEdicao = todosCampeonatos.filter(c => c.edicaoId === parseInt(id))
                setCampeonatos(campeonatosDaEdicao)
            } catch {
                setCampeonatos([])
            }

        } catch (err) {
            console.error('Erro ao carregar edição:', err)
            setError('Erro ao carregar dados da edição')
        } finally {
            setLoading(false)
        }
    }

    const toggleTimeExpandido = (timeId) => {
        setTimeExpandido(timeExpandido === timeId ? null : timeId)
    }

    if (loading) {
        return (
            <div className="edicao-detalhe-page">
                <div className="loading-container">
                    <div className="loading-spinner"></div>
                    <p>Carregando edição...</p>
                </div>
            </div>
        )
    }

    if (error || !edicao) {
        return (
            <div className="edicao-detalhe-page">
                <div className="error-container">
                    <p>{error || 'Edição não encontrada'}</p>
                    <Link to="/" className="btn-voltar">← Voltar ao Início</Link>
                </div>
            </div>
        )
    }

    const isEmAndamento = edicao.emAndamento || campeonatos.some(c => c.status === 'EM_ANDAMENTO')

    return (
        <div className="edicao-detalhe-page">
            {/* Header */}
            <header className="edicao-header">
                <Link to="/" className="back-link">← Voltar ao Início</Link>
                <div className="edicao-info">
                    {isEmAndamento && <span className="status-badge em-andamento">EM ANDAMENTO</span>}
                    <h1>{edicao.nomeCompleto || edicao.nome}</h1>
                    <span className="edicao-ano">{edicao.ano}</span>
                    {edicao.descricao && <p className="edicao-descricao">{edicao.descricao}</p>}
                </div>
                <div className="edicao-stats">
                    <div className="stat-item">
                        <span className="stat-value">{escalacoes.length}</span>
                        <span className="stat-label">Times</span>
                    </div>
                    <div className="stat-item">
                        <span className="stat-value">{campeonatos.length}</span>
                        <span className="stat-label">Campeonatos</span>
                    </div>
                    <div className="stat-item">
                        <span className="stat-value">
                            {escalacoes.reduce((total, e) => total + (e.jogadores?.length || 0), 0)}
                        </span>
                        <span className="stat-label">Jogadores</span>
                    </div>
                </div>
            </header>

            <main className="edicao-content">
                {/* Campeonatos */}
                {campeonatos.length > 0 && (
                    <section className="section-campeonatos">
                        <h2>Campeonatos</h2>
                        <div className="campeonatos-grid">
                            {campeonatos.map(campeonato => (
                                <Link 
                                    key={campeonato.id} 
                                    to={`/campeonato/${campeonato.id}`}
                                    className="campeonato-card-link"
                                >
                                    <div className="campeonato-card">
                                        <span className={`status-badge status-${campeonato.status?.toLowerCase()}`}>
                                            {campeonato.status?.replace('_', ' ')}
                                        </span>
                                        <h3>{campeonato.nome}</h3>
                                        <p>{campeonato.descricao}</p>
                                        <div className="campeonato-meta">
                                            <span>{campeonato.numeroTimesInscritos || 0} times</span>
                                            <span className="ver-mais">Ver chave →</span>
                                        </div>
                                    </div>
                                </Link>
                            ))}
                        </div>
                    </section>
                )}

                {/* Times Participantes */}
                <section className="section-times">
                    <h2>Times Participantes</h2>
                    {escalacoes.length > 0 ? (
                        <div className="times-lista">
                            {escalacoes.map(escalacao => (
                                <div key={escalacao.id} className="time-card">
                                    <div 
                                        className="time-header"
                                        onClick={() => toggleTimeExpandido(escalacao.id)}
                                    >
                                        <div className="time-info">
                                            <h3>{escalacao.time?.nomeTime || 'Time'}</h3>
                                            <span className="time-jogadores-count">
                                                {escalacao.jogadores?.length || 0} jogadores
                                            </span>
                                        </div>
                                        <span className={`expand-icon ${timeExpandido === escalacao.id ? 'expanded' : ''}`}>
                                            ▼
                                        </span>
                                    </div>
                                    
                                    {timeExpandido === escalacao.id && (
                                        <div className="time-jogadores">
                                            {escalacao.jogadores && escalacao.jogadores.length > 0 ? (
                                                <ul className="jogadores-lista">
                                                    {escalacao.jogadores.map(jogador => (
                                                        <li key={jogador.id} className="jogador-item">
                                                            <span className="jogador-nick">{jogador.nick}</span>
                                                            <span className="jogador-lane">{jogador.lane}</span>
                                                            {escalacao.capitao?.id === jogador.id && (
                                                                <span className="capitao-badge">👑 Capitão</span>
                                                            )}
                                                        </li>
                                                    ))}
                                                </ul>
                                            ) : (
                                                <p className="sem-jogadores">Nenhum jogador cadastrado</p>
                                            )}
                                        </div>
                                    )}
                                </div>
                            ))}
                        </div>
                    ) : (
                        <div className="sem-times">
                            <p>Nenhum time participante cadastrado para esta edição.</p>
                        </div>
                    )}
                </section>

                {/* Campeão (se houver) */}
                {campeonatos.some(c => c.campeao) && (
                    <section className="section-campeao">
                        <div className="campeao-card">
                            <span className="campeao-trophy">🏆</span>
                            <h2>Campeão</h2>
                            <p className="campeao-nome">
                                {campeonatos.find(c => c.campeao)?.campeao?.nomeTime}
                            </p>
                        </div>
                    </section>
                )}
            </main>
        </div>
    )
}

export default EdicaoDetalhe
