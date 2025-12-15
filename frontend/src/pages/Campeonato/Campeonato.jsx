import { useState, useEffect } from 'react'
import { useParams, Link } from 'react-router-dom'
import { campeonatosApi, partidasApi } from '../../services/api'
import './Campeonato.css'

function Campeonato() {
    const { id } = useParams()
    const [campeonato, setCampeonato] = useState(null)
    const [partidas, setPartidas] = useState([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState(null)
    const [success, setSuccess] = useState(null)
    const [faseAtiva, setFaseAtiva] = useState(null)

    // Modal de resultado
    const [partidaEditando, setPartidaEditando] = useState(null)
    const [placar1, setPlacar1] = useState('')
    const [placar2, setPlacar2] = useState('')

    useEffect(() => {
        loadCampeonato()
    }, [id])

    const loadCampeonato = async () => {
        setLoading(true)
        try {
            const campeonatoData = await campeonatosApi.buscarPorId(id)
            setCampeonato(campeonatoData)

            // Carrega partidas do campeonato
            const partidasData = await partidasApi.listarPorCampeonato(id)
            setPartidas(partidasData || [])

            // Define fase ativa como a primeira não finalizada ou a última
            if (campeonatoData.fases?.length > 0) {
                const faseNaoFinalizada = campeonatoData.fases.find(f => !f.finalizada)
                setFaseAtiva(faseNaoFinalizada?.id || campeonatoData.fases[campeonatoData.fases.length - 1].id)
            }
        } catch (err) {
            setError('Erro ao carregar campeonato')
            console.error(err)
        } finally {
            setLoading(false)
        }
    }

    const getPartidasPorFase = (faseId) => {
        return partidas.filter(p => p.faseId === faseId)
    }

    const getPartidasPorGrupo = (grupoId) => {
        return partidas.filter(p => p.grupoId === grupoId)
    }

    // Abrir modal de edição
    const abrirEdicao = (partida) => {
        setPartidaEditando(partida)
        setPlacar1(partida.placarTime1 !== null ? partida.placarTime1.toString() : '')
        setPlacar2(partida.placarTime2 !== null ? partida.placarTime2.toString() : '')
    }

    const fecharEdicao = () => {
        setPartidaEditando(null)
        setPlacar1('')
        setPlacar2('')
    }

    const salvarResultado = async () => {
        if (placar1 === '' || placar2 === '') {
            setError('Preencha os dois placares')
            setTimeout(() => setError(null), 3000)
            return
        }

        try {
            await partidasApi.registrarResultado(partidaEditando.id, {
                placarTime1: parseInt(placar1),
                placarTime2: parseInt(placar2)
            })
            setSuccess('Resultado salvo!')
            setTimeout(() => setSuccess(null), 3000)
            fecharEdicao()
            loadCampeonato()
        } catch (err) {
            setError(err.mensagem || 'Erro ao salvar resultado')
            setTimeout(() => setError(null), 5000)
        }
    }

    const handleAvancarFase = async () => {
        if (!window.confirm('Tem certeza que deseja encerrar esta fase e iniciar a próxima? Esta ação não pode ser desfeita.')) {
            return
        }

        try {
            setLoading(true)
            await campeonatosApi.avancarFase(id)
            setSuccess('Fase avançada com sucesso!')
            setTimeout(() => setSuccess(null), 3000)
            await loadCampeonato()
        } catch (err) {
            console.error(err)
            setError(err.message || 'Erro ao avançar fase. Verifique se todos os jogos estão finalizados.')
            setTimeout(() => setError(null), 5000)
        } finally {
            setLoading(false)
        }
    }

    if (loading) {
        return (
            <div className="campeonato-page">
                <div className="loading-full">Carregando campeonato...</div>
            </div>
        )
    }

    if (error && !campeonato) {
        return (
            <div className="campeonato-page">
                <div className="error-full">
                    <p>{error || 'Campeonato não encontrado'}</p>
                    <Link to="/admin" className="btn btn-secondary">← Voltar</Link>
                </div>
            </div>
        )
    }

    const faseAtual = campeonato?.fases?.find(f => f.id === faseAtiva)
    const partidasDaFase = faseAtual ? getPartidasPorFase(faseAtual.id) : []

    return (
        <div className="campeonato-page">
            {/* Alerts */}
            {error && <div className="alert-float alert-error">{error}</div>}
            {success && <div className="alert-float alert-success">{success}</div>}

            {/* Modal de Edição */}
            {partidaEditando && (
                <div className="modal-overlay" onClick={fecharEdicao}>
                    <div className="modal-content" onClick={e => e.stopPropagation()}>
                        <h3>Registrar Resultado</h3>
                        <div className="resultado-form">
                            <div className="time-placar-input">
                                <span className="time-nome">{partidaEditando.time1?.nomeTime || 'TBD'}</span>
                                <input
                                    type="number"
                                    min="0"
                                    value={placar1}
                                    onChange={e => setPlacar1(e.target.value)}
                                    placeholder="0"
                                />
                            </div>
                            <span className="vs-text">VS</span>
                            <div className="time-placar-input">
                                <input
                                    type="number"
                                    min="0"
                                    value={placar2}
                                    onChange={e => setPlacar2(e.target.value)}
                                    placeholder="0"
                                />
                                <span className="time-nome">{partidaEditando.time2?.nomeTime || 'TBD'}</span>
                            </div>
                        </div>
                        <div className="modal-actions">
                            <button onClick={fecharEdicao} className="btn btn-secondary">Cancelar</button>
                            <button onClick={salvarResultado} className="btn btn-primary">Salvar Resultado</button>
                        </div>
                    </div>
                </div>
            )}

            {/* Header */}
            <header className="campeonato-header">
                <Link to="/admin" className="back-link">← Voltar ao Painel</Link>
                <div className="campeonato-info">
                    <span className={`status-badge status-${campeonato.status?.toLowerCase()}`}>
                        {campeonato.status}
                    </span>
                    <h1>{campeonato.nome}</h1>
                    <p>{campeonato.descricao}</p>
                </div>
                <div className="campeonato-stats">
                    <div className="stat-item">
                        <span className="stat-value">{campeonato.numeroTimesInscritos || 0}</span>
                        <span className="stat-label">Times</span>
                    </div>
                    <div className="stat-item">
                        <span className="stat-value">{campeonato.fases?.length || 0}</span>
                        <span className="stat-label">Fases</span>
                    </div>
                    <div className="stat-item">
                        <span className="stat-value">{partidas.length}</span>
                        <span className="stat-label">Partidas</span>
                    </div>
                </div>
            </header>

            {/* Fases Tabs */}
            {campeonato.fases?.length > 0 && (
                <div className="fases-tabs">
                    {campeonato.fases.map((fase, index) => (
                        <button
                            key={fase.id}
                            className={`fase-tab ${faseAtiva === fase.id ? 'active' : ''} ${fase.finalizada ? 'finalizada' : ''}`}
                            onClick={() => setFaseAtiva(fase.id)}
                        >
                            <span className="fase-number">{index + 1}</span>
                            <span className="fase-nome">{fase.nome}</span>
                            {fase.finalizada && <span className="fase-check">✓</span>}
                        </button>
                    ))}
                </div>
            )}

            {/* Conteúdo da Fase */}
            <div className="fase-content">
                {faseAtual ? (
                    <>
                        <div className="fase-header">
                            <h2>{faseAtual.nome}</h2>
                            <span className="formato-badge">{faseAtual.formato}</span>

                            {/* Botão de Avançar Fase */}
                            {!faseAtual.finalizada &&
                                partidasDaFase.length > 0 &&
                                partidasDaFase.every(p => p.status === 'FINALIZADA' || p.status === 'WO') && (
                                    <button
                                        className="btn btn-warning btn-avancar"
                                        onClick={handleAvancarFase}
                                        style={{ marginLeft: 'auto' }}
                                    >
                                        Encerrar Fase e Avançar ⏩
                                    </button>
                                )}
                        </div>

                        {/* Grupos (se existirem) */}
                        {faseAtual.grupos?.length > 0 ? (
                            <div className="grupos-container">
                                {faseAtual.grupos.map(grupo => (
                                    <div key={grupo.id} className="grupo-card">
                                        <h3>{grupo.nome}</h3>

                                        {/* Tabela de Classificação */}
                                        <div className="classificacao-table">
                                            <div className="table-header">
                                                <span className="col-pos">#</span>
                                                <span className="col-time">Time</span>
                                                <span className="col-pts">Pts</span>
                                                <span className="col-v">V</span>
                                                <span className="col-d">D</span>
                                            </div>
                                            {grupo.classificacao?.sort((a, b) => b.pontos - a.pontos).map((c, i) => (
                                                <div key={c.time?.id} className={`table-row ${i < (faseAtual.classificadosNecessarios || 2) ? 'classificado' : ''}`}>
                                                    <span className="col-pos">{i + 1}</span>
                                                    <span className="col-time">{c.time?.nomeTime || 'Time Removido'}</span>
                                                    <span className="col-pts">{c.pontos}</span>
                                                    <span className="col-v">{c.vitorias}</span>
                                                    <span className="col-d">{c.derrotas}</span>
                                                </div>
                                            ))}
                                        </div>

                                        {/* Partidas do Grupo */}
                                        <div className="grupo-partidas">
                                            <h4>Partidas</h4>
                                            {getPartidasPorGrupo(grupo.id).map(partida => (
                                                <PartidaCard key={partida.id} partida={partida} onEdit={abrirEdicao} />
                                            ))}
                                        </div>
                                    </div>
                                ))}
                            </div>
                        ) : faseAtual.formato === 'SISTEMA_SUICO' ? (
                            <div className="swiss-container">
                                {/* Tabela Suíço */}
                                <div className="grupo-card">
                                    <h3>Classificação Geral</h3>
                                    <div className="classificacao-table">
                                        <div className="table-header">
                                            <span className="col-pos">#</span>
                                            <span className="col-time">Time</span>
                                            <span className="col-pts">Pts</span>
                                            <span className="col-v">V</span>
                                            <span className="col-d">D</span>
                                        </div>
                                        {calcularTabelaSuico(partidasDaFase, campeonato.timesParticipantes || []).map((c, i) => (
                                            <div key={c.timeId} className={`table-row ${i < (faseAtual.classificadosNecessarios || 8) ? 'classificado' : ''}`}>
                                                <span className="col-pos">{i + 1}</span>
                                                <span className="col-time">{c.nomeTime}</span>
                                                <span className="col-pts">{c.pontos}</span>
                                                <span className="col-v">{c.vitorias}</span>
                                                <span className="col-d">{c.derrotas}</span>
                                            </div>
                                        ))}
                                    </div>
                                </div>

                                {/* Partidas por Rodada */}
                                <div className="swiss-matches">
                                    {organizarBracket(partidasDaFase).map((rodada, i) => (
                                        <div key={i} className="bracket-round swiss-round">
                                            <h4 className="round-title">Rodada {rodada[0]?.rodada || (i + 1)}</h4>
                                            <div className="round-matches">
                                                {rodada.map(partida => (
                                                    <PartidaCard key={partida.id} partida={partida} onEdit={abrirEdicao} compact />
                                                ))}
                                            </div>
                                        </div>
                                    ))}
                                    {partidasDaFase.length === 0 && <p className="no-partidas">Aguardando geração de partidas.</p>}
                                </div>
                            </div>
                        ) : faseAtual.formato === 'LOSER_BRACKET' ? (
                            /* Double Elimination Bracket */
                            <div className="double-elimination-container">
                                {partidasDaFase.length === 0 ? (
                                    <p className="no-partidas">Nenhuma partida gerada para esta fase ainda.</p>
                                ) : (
                                    <>
                                        {/* Winners Bracket + Grand Finals */}
                                        {(() => {
                                            const winnersPartidas = partidasDaFase.filter(p => p.rodada < 100 && p.rodada > 0)
                                            const grandFinals = partidasDaFase.filter(p => p.identificadorBracket?.startsWith('GF'))
                                            const todasWinners = [...winnersPartidas, ...grandFinals]

                                            return todasWinners.length > 0 && (
                                                <div className="bracket-section winners-bracket">
                                                    <div className="bracket-section-header">
                                                        <span className="bracket-label">winner</span>
                                                        <span className="bracket-badge">WINNERS_BRACKET</span>
                                                    </div>
                                                    <div className="bracket-grid">
                                                        {/* Rodadas do WB */}
                                                        {organizarBracketWinners(winnersPartidas).map((rodada, rodadaIndex) => (
                                                            <div key={rodadaIndex} className="bracket-round">
                                                                <h4 className="round-title">{getWinnersRoundName(rodada[0]?.rodada, winnersPartidas)}</h4>
                                                                <div className="round-matches">
                                                                    {rodada.map(partida => (
                                                                        <PartidaCard key={partida.id} partida={partida} onEdit={abrirEdicao} compact />
                                                                    ))}
                                                                </div>
                                                            </div>
                                                        ))}
                                                        {/* Grand Finals como última coluna */}
                                                        {grandFinals.length > 0 && (
                                                            <div className="bracket-round grand-finals-round">
                                                                <h4 className="round-title grand">Grand Finals</h4>
                                                                <div className="round-matches">
                                                                    {grandFinals.map(partida => (
                                                                        <PartidaCard key={partida.id} partida={partida} onEdit={abrirEdicao} compact />
                                                                    ))}
                                                                </div>
                                                            </div>
                                                        )}
                                                    </div>
                                                </div>
                                            )
                                        })()}

                                        {/* Losers Bracket */}
                                        {(() => {
                                            const losersPartidas = partidasDaFase.filter(p => p.rodada >= 100)
                                            return losersPartidas.length > 0 && (
                                                <div className="bracket-section losers-bracket">
                                                    <div className="bracket-section-header">
                                                        <span className="bracket-label">loser</span>
                                                        <span className="bracket-badge">LOSER_BRACKET</span>
                                                    </div>
                                                    <div className="bracket-grid">
                                                        {organizarBracketLosers(losersPartidas).map((rodada, rodadaIndex) => (
                                                            <div key={rodadaIndex} className="bracket-round">
                                                                <h4 className="round-title">{getLosersRoundName(rodada[0]?.rodada, organizarBracketLosers(losersPartidas).length, rodadaIndex)}</h4>
                                                                <div className="round-matches">
                                                                    {rodada.map(partida => (
                                                                        <PartidaCard key={partida.id} partida={partida} onEdit={abrirEdicao} compact />
                                                                    ))}
                                                                </div>
                                                            </div>
                                                        ))}
                                                    </div>
                                                </div>
                                            )
                                        })()}
                                    </>
                                )}
                            </div>
                        ) : (
                            /* Bracket Mata-Mata */
                            <div className="bracket-container">
                                {partidasDaFase.length === 0 ? (
                                    <p className="no-partidas">Nenhuma partida gerada para esta fase ainda.</p>
                                ) : (
                                    <div className="bracket-grid">
                                        {organizarBracket(partidasDaFase).map((rodada, rodadaIndex) => (
                                            <div key={rodadaIndex} className="bracket-round">
                                                <h4 className="round-title">{getRoundName(rodadaIndex, organizarBracket(partidasDaFase).length)}</h4>
                                                <div className="round-matches">
                                                    {rodada.map(partida => (
                                                        <PartidaCard key={partida.id} partida={partida} onEdit={abrirEdicao} compact />
                                                    ))}
                                                </div>
                                            </div>
                                        ))}
                                    </div>
                                )}
                            </div>
                        )}
                    </>
                ) : (
                    <div className="no-fase">
                        <p>Este campeonato ainda não possui fases configuradas.</p>
                    </div>
                )}
            </div>

            {/* Campeão (se finalizado) */}
            {
                campeonato.status === 'FINALIZADO' && campeonato.campeao && (
                    <div className="campeao-section">
                        <div className="campeao-card">
                            <span className="campeao-trophy">🏆</span>
                            <h2>Campeão</h2>
                            <p className="campeao-nome">{campeonato.campeao.nomeTime}</p>
                        </div>
                    </div>
                )
            }
        </div >
    )
}

// Componente de Partida
function PartidaCard({ partida, compact = false, onEdit }) {
    const isFinalizada = partida.status === 'FINALIZADA'
    const status = isFinalizada ? 'finalizada' : (partida.placarTime1 !== null ? 'em-andamento' : 'pendente')

    return (
        <div
            className={`partida-card ${status} ${compact ? 'compact' : ''} clickable`}
            onClick={() => onEdit(partida)}
            title="Clique para editar resultado"
        >
            <div className="partida-time time1">
                <span className="time-nome">{partida.time1?.nomeTime || 'TBD'}</span>
                <span className={`time-placar ${isFinalizada && partida.placarTime1 > partida.placarTime2 ? 'winner' : ''}`}>
                    {partida.placarTime1 ?? '-'}
                </span>
            </div>
            <div className="partida-vs">VS</div>
            <div className="partida-time time2">
                <span className={`time-placar ${isFinalizada && partida.placarTime2 > partida.placarTime1 ? 'winner' : ''}`}>
                    {partida.placarTime2 ?? '-'}
                </span>
                <span className="time-nome">{partida.time2?.nomeTime || 'TBD'}</span>
            </div>
            {isFinalizada && (
                <div className="partida-check">✓</div>
            )}
            <div className="edit-hint">✏️</div>
        </div>
    )
}

// Organiza partidas em rodadas para bracket
function organizarBracket(partidas) {
    const rodadas = []
    const porRodada = {}

    partidas.forEach(p => {
        const rodada = p.rodada || 1
        if (!porRodada[rodada]) porRodada[rodada] = []
        porRodada[rodada].push(p)
    })

    Object.keys(porRodada).sort((a, b) => a - b).forEach(rodada => {
        rodadas.push(porRodada[rodada])
    })

    return rodadas.length > 0 ? rodadas : (partidas.length > 0 ? [[...partidas]] : [])
}

// Organiza partidas do Winners Bracket (ordem decrescente: Quartas -> Semifinal -> Final)
function organizarBracketWinners(partidas) {
    const rodadas = []
    const porRodada = {}

    partidas.forEach(p => {
        const rodada = p.rodada || 1
        if (!porRodada[rodada]) porRodada[rodada] = []
        porRodada[rodada].push(p)
    })

    // Ordena em ordem DECRESCENTE (maior para menor)
    Object.keys(porRodada).sort((a, b) => b - a).forEach(rodada => {
        rodadas.push(porRodada[rodada])
    })

    return rodadas.length > 0 ? rodadas : (partidas.length > 0 ? [[...partidas]] : [])
}

// Organiza partidas do Losers Bracket
function organizarBracketLosers(partidas) {
    const rodadas = []
    const porRodada = {}

    partidas.forEach(p => {
        // Rodada do LB é 100 + numero real da rodada
        const rodada = p.rodada || 101
        if (!porRodada[rodada]) porRodada[rodada] = []
        porRodada[rodada].push(p)
    })

    Object.keys(porRodada).sort((a, b) => a - b).forEach(rodada => {
        rodadas.push(porRodada[rodada])
    })

    return rodadas.length > 0 ? rodadas : (partidas.length > 0 ? [[...partidas]] : [])
}

// Nome das rodadas do Winners Bracket (baseado no número real da rodada)
function getWinnersRoundName(rodada, todasPartidas) {
    // No WB do double elim: rodada maior = primeira rodada, rodada 1 = Final WB
    // Rodada 1 = Final WB (antes da Grand Finals)
    // Rodada 2 = Semifinal
    // Rodada 3 = Quartas
    // Rodada 4 = Oitavas
    switch (rodada) {
        case 1: return 'Final WB'
        case 2: return 'Semifinal'
        case 3: return 'Quartas'
        case 4: return 'Oitavas'
        case 5: return '16 avos'
        default: return `Rodada ${rodada}`
    }
}

// Nome das rodadas do Losers Bracket
function getLosersRoundName(rodada, total, index) {
    const rodadaReal = (rodada || 101) - 100
    if (total === 1) return 'Final do Losers'
    const fromEnd = total - index
    switch (fromEnd) {
        case 1: return 'Final LB'
        case 2: return 'Semifinal LB'
        case 3: return 'Quartas LB'
        default: return `Rodada ${rodadaReal}`
    }
}

// Nome das rodadas
function getRoundName(index, total) {
    if (total === 1) return 'Partidas'
    const fromEnd = total - index
    switch (fromEnd) {
        case 1: return 'Final'
        case 2: return 'Semifinal'
        case 3: return 'Quartas'
        case 4: return 'Oitavas'
        default: return `Rodada ${index + 1}`
    }
}

// Calcula tabela para sistema suíço
function calcularTabelaSuico(partidas, times) {
    const stats = {}

    // Inicializa
    if (!times) return []
    times.forEach(time => {
        stats[time.id] = {
            timeId: time.id,
            nomeTime: time.nomeTime,
            pontos: 0,
            vitorias: 0,
            derrotas: 0
        }
    })

    partidas.forEach(p => {
        if (p.status === 'FINALIZADA' && p.vencedor) {
            // Vencedor
            if (stats[p.vencedor.id]) {
                stats[p.vencedor.id].pontos += 3
                stats[p.vencedor.id].vitorias += 1
            }
            // Perdedor
            const perdedorId = p.time1.id === p.vencedor.id ? p.time2?.id : p.time1.id
            if (perdedorId && stats[perdedorId]) {
                stats[perdedorId].derrotas += 1
            }
        }
    })

    return Object.values(stats).sort((a, b) => b.pontos - a.pontos)
}

export default Campeonato
