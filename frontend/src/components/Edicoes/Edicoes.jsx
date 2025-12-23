import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { edicoesApi, campeonatosApi } from '../../services/api'
import './Edicoes.css'

// Dados de fallback caso a API não esteja disponível
const edicoesFallback = [
    {
        id: 1,
        ano: 2024,
        numeroEdicao: 1,
        nome: 'CBPitu V',
        nomeCompleto: 'CBPitu V',
        descricao: 'A maior edição de todas. 8 times, novo formato com fase de grupos + playoffs.',
        emAndamento: true,
        campeao: null
    },
    {
        id: 2,
        ano: 2023,
        numeroEdicao: 1,
        nome: 'CBPitu IV',
        nomeCompleto: 'CBPitu IV',
        descricao: '6 times disputaram o título. Final épica decidida no game 5.',
        emAndamento: false,
        campeao: 'Nome do Time Campeão'
    },
    {
        id: 3,
        ano: 2022,
        numeroEdicao: 1,
        nome: 'CBPitu III',
        nomeCompleto: 'CBPitu III',
        descricao: 'Primeira edição com formato mata-mata. Surpresas e upsets marcaram o torneio.',
        emAndamento: false,
        campeao: 'Nome do Time Campeão'
    }
]

function EdicaoCard({ edicao, campeonatos }) {
    // Verifica se a edição está em andamento baseado nas datas ou se há campeonato ativo
    const isEmAndamento = edicao.emAndamento ||
        (campeonatos && campeonatos.some(c => c.status === 'EM_ANDAMENTO'))

    // Pega o campeão do campeonato finalizado, se houver
    const campeaoNome = edicao.campeao ||
        (campeonatos && campeonatos.find(c => c.campeao)?.campeao?.nomeTime)

    // Campeonato principal da edição (para link rápido)
    const campeonatoPrincipal = campeonatos && campeonatos[0]

    return (
        <Link to={`/edicao/${edicao.id}`} className="edicao-card-link">
            <div className={`edicao-card ${isEmAndamento ? 'current' : ''}`}>
                {isEmAndamento && <div className="edicao-badge">EM ANDAMENTO</div>}
                <h3>{edicao.nomeCompleto || edicao.nome}</h3>
                <p>{edicao.descricao || `Edição ${edicao.numeroEdicao} do ano de ${edicao.ano}`}</p>

                {isEmAndamento ? (
                    <div className="edicao-actions">
                        {campeonatoPrincipal && (
                            <span
                                className="btn-ver-campeonato"
                                onClick={(e) => {
                                    e.preventDefault()
                                    e.stopPropagation()
                                    window.location.href = `/campeonato/${campeonatoPrincipal.id}`
                                }}
                            >
                                Ver Campeonato →
                            </span>
                        )}
                    </div>
                ) : (
                    <div className="edicao-champion">
                        <span>🏆 Campeão:</span>
                        <strong>{campeaoNome || 'A definir'}</strong>
                    </div>
                )}

                {edicao.numeroEscalacoes > 0 && (
                    <div className="edicao-stats">
                        <span>{edicao.numeroEscalacoes} times participantes</span>
                    </div>
                )}

                <div className="edicao-ver-detalhes">
                    Ver detalhes da edição →
                </div>
            </div>
        </Link>
    )
}

function Edicoes() {
    const [edicoes, setEdicoes] = useState([])
    const [campeonatosPorEdicao, setCampeonatosPorEdicao] = useState({})
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState(null)

    useEffect(() => {
        async function carregarDados() {
            try {
                setLoading(true)

                // Tenta carregar edições da API
                const edicoesData = await edicoesApi.listar()

                if (edicoesData && edicoesData.length > 0) {
                    setEdicoes(edicoesData)

                    // Carrega campeonatos para verificar status
                    try {
                        const campeonatos = await campeonatosApi.listar()
                        const porEdicao = {}
                        campeonatos.forEach(c => {
                            if (c.edicaoId) {
                                if (!porEdicao[c.edicaoId]) {
                                    porEdicao[c.edicaoId] = []
                                }
                                porEdicao[c.edicaoId].push(c)
                            }
                        })
                        setCampeonatosPorEdicao(porEdicao)
                    } catch {
                        // Ignora erro ao carregar campeonatos
                    }
                } else {
                    // Usa dados de fallback se não houver edições
                    setEdicoes(edicoesFallback)
                }

                setError(null)
            } catch (err) {
                console.warn('API de edições não disponível, usando dados de fallback:', err)
                setEdicoes(edicoesFallback)
                setError(null) // Não mostra erro, usa fallback silenciosamente
            } finally {
                setLoading(false)
            }
        }

        carregarDados()
    }, [])

    // Agrupa edições por ano para exibição
    const edicoesPorAno = edicoes.reduce((acc, edicao) => {
        const ano = edicao.ano
        if (!acc[ano]) {
            acc[ano] = []
        }
        acc[ano].push(edicao)
        return acc
    }, {})

    // Ordena anos de forma decrescente
    const anosOrdenados = Object.keys(edicoesPorAno).sort((a, b) => b - a)

    if (loading) {
        return (
            <section id="edicoes" className="section edicoes">
                <div className="container">
                    <div className="section-header">
                        <span className="section-tag">Histórico</span>
                        <h2 className="section-title">Conheça as <span className="gradient-text">Edições</span></h2>
                        <p className="section-description">Carregando...</p>
                    </div>
                </div>
            </section>
        )
    }

    return (
        <section id="edicoes" className="section edicoes">
            <div className="container">
                <div className="section-header">
                    <span className="section-tag">Histórico</span>
                    <h2 className="section-title">Conheça as <span className="gradient-text">Edições</span></h2>
                    <p className="section-description">A história do campeonato ao longo dos anos</p>
                </div>

                {error && (
                    <div className="edicoes-error">
                        <p>{error}</p>
                    </div>
                )}

                <div className="timeline">
                    {anosOrdenados.map(ano => (
                        edicoesPorAno[ano].map((edicao, index) => (
                            <div key={edicao.id || `${ano}-${index}`} className="timeline-item">
                                <div className="timeline-marker">
                                    <span className="timeline-year">{ano}</span>
                                </div>
                                <div className="timeline-content">
                                    <EdicaoCard
                                        edicao={edicao}
                                        campeonatos={campeonatosPorEdicao[edicao.id]}
                                    />
                                </div>
                            </div>
                        ))
                    ))}
                </div>
            </div>
        </section>
    )
}

export default Edicoes
