import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { jogadoresApi, timesApi, campeonatosApi, edicoesApi } from '../../services/api'
import './Admin.css'

function Admin() {
    const [activeTab, setActiveTab] = useState('jogadores')
    const [jogadores, setJogadores] = useState([])
    const [times, setTimes] = useState([])
    const [campeonatos, setCampeonatos] = useState([])
    const [edicoes, setEdicoes] = useState([])
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState(null)
    const [success, setSuccess] = useState(null)

    // Modal states
    const [selectedTime, setSelectedTime] = useState(null)
    const [selectedCampeonato, setSelectedCampeonato] = useState(null)

    // Forms state
    const [novoJogador, setNovoJogador] = useState({ nickname: '', nomeReal: '', laneLol: 'MID' })
    const [novoTime, setNovoTime] = useState({ nomeTime: '', trofeus: '' })
    const [novosJogadoresDoTime, setNovosJogadoresDoTime] = useState([
        { nickname: '', nomeReal: '', laneLol: 'TOP' },
        { nickname: '', nomeReal: '', laneLol: 'JUNGLE' },
        { nickname: '', nomeReal: '', laneLol: 'MID' },
        { nickname: '', nomeReal: '', laneLol: 'ADC' },
        { nickname: '', nomeReal: '', laneLol: 'SUPPORT' },
    ])
    const [novoCampeonato, setNovoCampeonato] = useState({ nome: '', descricao: '', limiteMaximoTimes: 8, edicaoId: '' })
    const [novaFase, setNovaFase] = useState({ nome: '', formato: 'MATA_MATA', classificadosNecessarios: 1, rodadasTotais: 1 })
    const [novaEdicao, setNovaEdicao] = useState({ ano: new Date().getFullYear(), numeroEdicao: 1, nome: '', descricao: '' })

    const lanes = ['TOP', 'JUNGLE', 'MID', 'ADC', 'SUPPORT']
    const formatos = ['MATA_MATA', 'MATA_MATA_MD3', 'MATA_MATA_MD5', 'GRUPOS', 'GRUPOS_IDA_VOLTA', 'SISTEMA_SUICO', 'LOSER_BRACKET']

    useEffect(() => {
        loadData()
    }, [activeTab])

    const loadData = async () => {
        setLoading(true)
        setError(null)
        try {
            const [jogadoresData, timesData, campeonatosData, edicoesData] = await Promise.all([
                jogadoresApi.listar().catch(() => []),
                timesApi.listar().catch(() => []),
                campeonatosApi.listar().catch(() => []),
                edicoesApi.listar().catch(() => [])
            ])
            setJogadores(jogadoresData || [])
            setTimes(timesData || [])
            setCampeonatos(campeonatosData || [])
            setEdicoes(edicoesData || [])
        } catch (err) {
            setError('Erro ao conectar com o backend. Verifique se a API está rodando.')
        } finally {
            setLoading(false)
        }
    }

    const showSuccess = (msg) => {
        setSuccess(msg)
        setTimeout(() => setSuccess(null), 3000)
    }

    const showError = (err) => {
        setError(err.mensagem || err.message || 'Erro desconhecido')
        setTimeout(() => setError(null), 5000)
    }

    // ==================== Jogadores ====================
    const criarJogador = async (e) => {
        e.preventDefault()
        try {
            await jogadoresApi.criar(novoJogador)
            setNovoJogador({ nickname: '', nomeReal: '', laneLol: 'MID' })
            showSuccess('Jogador criado com sucesso!')
            loadData()
        } catch (err) {
            showError(err)
        }
    }

    const deletarJogador = async (id) => {
        if (!confirm('Deletar este jogador?')) return
        try {
            await jogadoresApi.deletar(id)
            showSuccess('Jogador deletado!')
            loadData()
        } catch (err) {
            showError(err)
        }
    }

    // ==================== Times ====================
    const criarTimeComJogadores = async (e) => {
        e.preventDefault()
        try {
            // 1. Cria o time
            const timeCriado = await timesApi.criar(novoTime)

            // 2. Cria os jogadores que têm nickname preenchido e adiciona ao time
            let jogadoresCriados = 0
            for (const jogador of novosJogadoresDoTime) {
                if (jogador.nickname.trim()) {
                    try {
                        const jogadorCriado = await jogadoresApi.criar(jogador)
                        await timesApi.adicionarJogador(timeCriado.id, jogadorCriado.id)
                        jogadoresCriados++
                    } catch (err) {
                        console.error('Erro ao criar jogador:', err)
                    }
                }
            }

            // 3. Reset forms
            setNovoTime({ nomeTime: '', trofeus: '' })
            setNovosJogadoresDoTime([
                { nickname: '', nomeReal: '', laneLol: 'TOP' },
                { nickname: '', nomeReal: '', laneLol: 'JUNGLE' },
                { nickname: '', nomeReal: '', laneLol: 'MID' },
                { nickname: '', nomeReal: '', laneLol: 'ADC' },
                { nickname: '', nomeReal: '', laneLol: 'SUPPORT' },
            ])

            showSuccess(`Time criado com ${jogadoresCriados} jogadores!`)
            loadData()
        } catch (err) {
            showError(err)
        }
    }

    const updateJogadorDoTime = (index, field, value) => {
        const updated = [...novosJogadoresDoTime]
        updated[index] = { ...updated[index], [field]: value }
        setNovosJogadoresDoTime(updated)
    }

    const deletarTime = async (id) => {
        if (!confirm('Deletar este time?')) return
        try {
            await timesApi.deletar(id)
            showSuccess('Time deletado!')
            setSelectedTime(null)
            loadData()
        } catch (err) {
            showError(err)
        }
    }

    const adicionarJogadorAoTime = async (timeId, jogadorId) => {
        try {
            await timesApi.adicionarJogador(timeId, jogadorId)
            showSuccess('Jogador adicionado ao time!')
            loadData()
            const updatedTime = await timesApi.buscarPorId(timeId)
            setSelectedTime(updatedTime)
        } catch (err) {
            showError(err)
        }
    }

    const removerJogadorDoTime = async (timeId, jogadorId) => {
        try {
            await timesApi.removerJogador(timeId, jogadorId)
            showSuccess('Jogador removido do time!')
            loadData()
            const updatedTime = await timesApi.buscarPorId(timeId)
            setSelectedTime(updatedTime)
        } catch (err) {
            showError(err)
        }
    }

    const definirCapitao = async (timeId, jogadorId) => {
        try {
            await timesApi.definirCapitao(timeId, jogadorId)
            showSuccess('Capitão definido!')
            loadData()
            const updatedTime = await timesApi.buscarPorId(timeId)
            setSelectedTime(updatedTime)
        } catch (err) {
            showError(err)
        }
    }

    // ==================== Campeonatos ====================
    const criarCampeonato = async (e) => {
        e.preventDefault()
        try {
            // Prepara o payload, convertendo edicaoId vazio para null
            const payload = {
                ...novoCampeonato,
                edicaoId: novoCampeonato.edicaoId ? Number(novoCampeonato.edicaoId) : null
            }
            await campeonatosApi.criar(payload)
            setNovoCampeonato({ nome: '', descricao: '', limiteMaximoTimes: 8, edicaoId: '' })
            showSuccess('Campeonato criado com sucesso!')
            loadData()
        } catch (err) {
            showError(err)
        }
    }

    const deletarCampeonato = async (id) => {
        if (!confirm('Deletar este campeonato?')) return
        try {
            await campeonatosApi.deletar(id)
            showSuccess('Campeonato deletado!')
            setSelectedCampeonato(null)
            loadData()
        } catch (err) {
            showError(err)
        }
    }

    const abrirInscricoes = async (id) => {
        try {
            await campeonatosApi.abrirInscricoes(id)
            showSuccess('Inscrições abertas!')
            loadData()
            const updated = await campeonatosApi.buscarPorId(id)
            setSelectedCampeonato(updated)
        } catch (err) {
            showError(err)
        }
    }

    const fecharInscricoes = async (id) => {
        try {
            await campeonatosApi.fecharInscricoes(id)
            showSuccess('Inscrições fechadas!')
            loadData()
            const updated = await campeonatosApi.buscarPorId(id)
            setSelectedCampeonato(updated)
        } catch (err) {
            showError(err)
        }
    }

    const inscreverTime = async (campeonatoId, timeId) => {
        try {
            await campeonatosApi.inscreverTime(campeonatoId, timeId)
            showSuccess('Time inscrito!')
            loadData()
            const updated = await campeonatosApi.buscarPorId(campeonatoId)
            setSelectedCampeonato(updated)
        } catch (err) {
            showError(err)
        }
    }

    const adicionarFase = async (campeonatoId) => {
        try {
            await campeonatosApi.adicionarFase(campeonatoId, novaFase)
            setNovaFase({ nome: '', formato: 'MATA_MATA', classificadosNecessarios: 1, rodadasTotais: 1 })
            showSuccess('Fase adicionada!')
            const updated = await campeonatosApi.buscarPorId(campeonatoId)
            setSelectedCampeonato(updated)
        } catch (err) {
            showError(err)
        }
    }

    const iniciarCampeonato = async (id) => {
        try {
            await campeonatosApi.iniciar(id)
            showSuccess('Campeonato iniciado!')
            loadData()
            const updated = await campeonatosApi.buscarPorId(id)
            setSelectedCampeonato(updated)
        } catch (err) {
            showError(err)
        }
    }

    // Manual Matchmaking API (Pairs & Groups)
    const [manualMatchMode, setManualMatchMode] = useState(false)
    const [manualPairs, setManualPairs] = useState([])
    const [manualGroups, setManualGroups] = useState([]) // [{ nome: 'Grupo A', timesIds: [] }]
    const [targetFase, setTargetFase] = useState(null) // Stores full phase object

    const startManualMatchmaking = (fase) => {
        setTargetFase(fase)
        setManualMatchMode(true)

        if (fase.formato.startsWith('MATA_MATA') || fase.formato === 'LOSER_BRACKET') {
            // Inicializa com um par vazio
            setManualPairs([{ time1Id: '', time2Id: '' }])
            setManualGroups([])
        } else if (fase.formato.startsWith('GRUPOS')) {
            // Inicializa com 2 grupos padrão
            setManualGroups([
                { nome: 'Grupo A', timesIds: [] },
                { nome: 'Grupo B', timesIds: [] }
            ])
            setManualPairs([])
        }
    }

    const addManualPair = () => {
        setManualPairs([...manualPairs, { time1Id: '', time2Id: '' }])
    }

    const removeManualPair = (index) => {
        const updated = [...manualPairs]
        updated.splice(index, 1)
        setManualPairs(updated)
    }

    const updateManualPair = (index, field, value) => {
        const updated = [...manualPairs]
        updated[index] = { ...updated[index], [field]: value }
        setManualPairs(updated)
    }

    // Gerenciamento de Grupos Manuais
    const addManualGroup = () => {
        const nextLetter = String.fromCharCode(65 + manualGroups.length); // A, B, C...
        setManualGroups([...manualGroups, { nome: `Grupo ${nextLetter}`, timesIds: [] }])
    }

    const removeManualGroup = (index) => {
        const updated = [...manualGroups]
        updated.splice(index, 1)
        setManualGroups(updated)
    }

    const updateGroupName = (index, name) => {
        const updated = [...manualGroups]
        updated[index] = { ...updated[index], nome: name }
        setManualGroups(updated)
    }

    const toggleTeamInGroup = (groupIndex, timeId) => {
        const updated = [...manualGroups]
        const group = updated[groupIndex]
        const id = Number(timeId)

        if (group.timesIds.includes(id)) {
            // Remove
            group.timesIds = group.timesIds.filter(tid => tid !== id)
        } else {
            // Add (check if already in another group first? Optional, but good UX)
            // Remove from other groups to ensure uniqueness
            updated.forEach(g => {
                g.timesIds = g.timesIds.filter(tid => tid !== id)
            })
            group.timesIds.push(id)
        }
        setManualGroups(updated)
    }

    const submitManualMatches = async () => {
        try {
            if (targetFase.formato.startsWith('MATA_MATA') || targetFase.formato === 'LOSER_BRACKET') {
                // Validate Pairs
                const invalidPairs = manualPairs.some(p => !p.time1Id || !p.time2Id || p.time1Id === p.time2Id)
                if (invalidPairs) {
                    showError('Preencha todos os pares com times diferentes.')
                    return
                }

                // Convert to DTO format
                const payload = manualPairs.map(p => ({
                    time1Id: Number(p.time1Id),
                    time2Id: Number(p.time2Id)
                }))

                await campeonatosApi.criarConfrontosManuais(selectedCampeonato.id, targetFase.id, payload)
                showSuccess('Confrontos criados manualmente!')

            } else if (targetFase.formato.startsWith('GRUPOS')) {
                // Validate Groups
                const invalidGroups = manualGroups.some(g => g.timesIds.length < 2)
                if (invalidGroups) {
                    showError('Cada grupo deve ter pelo menos 2 times.')
                    return
                }

                await campeonatosApi.criarGruposManuais(selectedCampeonato.id, targetFase.id, manualGroups)
                showSuccess('Grupos criados manualmente!')
            }

            setManualMatchMode(false)
            loadData()
            const updated = await campeonatosApi.buscarPorId(selectedCampeonato.id)
            setSelectedCampeonato(updated)
        } catch (err) {
            showError(err)
        }
    }

    // ==================== Edições ====================
    const criarEdicao = async (e) => {
        e.preventDefault()
        try {
            await edicoesApi.criar(novaEdicao)
            setNovaEdicao({ ano: new Date().getFullYear(), numeroEdicao: 1, nome: '', descricao: '' })
            showSuccess('Edição criada com sucesso!')
            loadData()
        } catch (err) {
            showError(err)
        }
    }

    const deletarEdicao = async (id) => {
        if (!confirm('Deletar esta edição?')) return
        try {
            await edicoesApi.deletar(id)
            showSuccess('Edição deletada!')
            loadData()
        } catch (err) {
            showError(err)
        }
    }

    // Jogadores disponíveis (não estão no time selecionado)
    const jogadoresDisponiveis = selectedTime
        ? jogadores.filter(j => !selectedTime.jogadores?.some(tj => tj.id === j.id))
        : []

    // Times disponíveis (não estão inscritos no campeonato)
    const timesDisponiveis = selectedCampeonato
        ? times.filter(t => !selectedCampeonato.timesInscritos?.some(ti => ti.id === t.id))
        : times

    return (
        <div className="admin-page">
            <div className="admin-header">
                <h1>🛠️ Painel de Testes - API CBPitu</h1>
                <p>Use este painel para testar a conexão com o backend</p>
            </div>

            {error && <div className="alert alert-error">{error}</div>}
            {success && <div className="alert alert-success">{success}</div>}

            <div className="admin-tabs">
                <button className={activeTab === 'jogadores' ? 'active' : ''} onClick={() => setActiveTab('jogadores')}>
                    👤 Jogadores ({jogadores.length})
                </button>
                <button className={activeTab === 'times' ? 'active' : ''} onClick={() => setActiveTab('times')}>
                    🎮 Times ({times.length})
                </button>
                <button className={activeTab === 'campeonatos' ? 'active' : ''} onClick={() => setActiveTab('campeonatos')}>
                    🏆 Campeonatos ({campeonatos.length})
                </button>
                <button className={activeTab === 'edicoes' ? 'active' : ''} onClick={() => setActiveTab('edicoes')}>
                    📅 Edições ({edicoes.length})
                </button>
            </div>

            <div className="admin-content">
                {loading && <div className="loading">Carregando...</div>}

                {/* ==================== JOGADORES ==================== */}
                {activeTab === 'jogadores' && (
                    <div className="tab-content">
                        <div className="form-section">
                            <h3>Criar Jogador</h3>
                            <form onSubmit={criarJogador}>
                                <input type="text" placeholder="Nickname" value={novoJogador.nickname}
                                    onChange={(e) => setNovoJogador({ ...novoJogador, nickname: e.target.value })} required />
                                <input type="text" placeholder="Nome Real" value={novoJogador.nomeReal}
                                    onChange={(e) => setNovoJogador({ ...novoJogador, nomeReal: e.target.value })} />
                                <select value={novoJogador.laneLol} onChange={(e) => setNovoJogador({ ...novoJogador, laneLol: e.target.value })}>
                                    {lanes.map(lane => <option key={lane} value={lane}>{lane}</option>)}
                                </select>
                                <button type="submit" className="btn btn-primary">Criar Jogador</button>
                            </form>
                        </div>
                        <div className="list-section">
                            <h3>Jogadores Cadastrados</h3>
                            {jogadores.length === 0 ? <p className="empty">Nenhum jogador</p> : (
                                <div className="data-grid">
                                    {jogadores.map(j => (
                                        <div key={j.id} className="data-card">
                                            <div className="data-card-header">
                                                <strong>{j.nickname}</strong>
                                                <span className="badge">{j.laneLol}</span>
                                            </div>
                                            <p>{j.nomeReal || '-'}</p>
                                            <button onClick={() => deletarJogador(j.id)} className="btn-delete">🗑️</button>
                                        </div>
                                    ))}
                                </div>
                            )}
                        </div>
                    </div>
                )}

                {/* ==================== TIMES ==================== */}
                {activeTab === 'times' && (
                    <div className="tab-content-full">
                        <div className="panel-left">
                            <div className="form-section form-section-large">
                                <h3>Criar Time com Jogadores</h3>
                                <form onSubmit={criarTimeComJogadores}>
                                    <div className="form-group">
                                        <label>Nome do Time *</label>
                                        <input type="text" placeholder="Ex: Pitu Gaming" value={novoTime.nomeTime}
                                            onChange={(e) => setNovoTime({ ...novoTime, nomeTime: e.target.value })} required />
                                    </div>
                                    <div className="form-group">
                                        <label>Troféus</label>
                                        <input type="text" placeholder="Ex: Bi-campeão 2022/2023" value={novoTime.trofeus}
                                            onChange={(e) => setNovoTime({ ...novoTime, trofeus: e.target.value })} />
                                    </div>

                                    <div className="form-divider">
                                        <span>Jogadores (opcional)</span>
                                    </div>

                                    <div className="jogadores-form-grid">
                                        {novosJogadoresDoTime.map((jogador, index) => (
                                            <div key={index} className="jogador-form-row">
                                                <span className="lane-label">{jogador.laneLol}</span>
                                                <input
                                                    type="text"
                                                    placeholder="Nickname"
                                                    value={jogador.nickname}
                                                    onChange={(e) => updateJogadorDoTime(index, 'nickname', e.target.value)}
                                                />
                                                <input
                                                    type="text"
                                                    placeholder="Nome Real"
                                                    value={jogador.nomeReal}
                                                    onChange={(e) => updateJogadorDoTime(index, 'nomeReal', e.target.value)}
                                                />
                                            </div>
                                        ))}
                                    </div>

                                    <button type="submit" className="btn btn-primary btn-full">Criar Time</button>
                                </form>
                            </div>

                            <div className="list-section">
                                <h3>Times ({times.length})</h3>
                                {times.length === 0 ? <p className="empty">Nenhum time</p> : (
                                    <div className="data-list">
                                        {times.map(t => (
                                            <div key={t.id} className={`data-item ${selectedTime?.id === t.id ? 'selected' : ''}`}
                                                onClick={() => setSelectedTime(t)}>
                                                <strong>{t.nomeTime}</strong>
                                                <span>{t.jogadores?.length || 0} jogadores</span>
                                                <button onClick={(e) => { e.stopPropagation(); deletarTime(t.id); }} className="btn-delete">🗑️</button>
                                            </div>
                                        ))}
                                    </div>
                                )}
                            </div>
                        </div>

                        {/* Detalhes do Time */}
                        <div className="panel-right">
                            {selectedTime ? (
                                <div className="detail-panel">
                                    <h3>📋 {selectedTime.nomeTime}</h3>
                                    <p className="detail-sub">Capitão: {selectedTime.capitao?.nickname || 'Não definido'}</p>

                                    <div className="detail-section">
                                        <h4>Jogadores do Time ({selectedTime.jogadores?.length || 0})</h4>
                                        {selectedTime.jogadores?.length === 0 ? (
                                            <p className="empty">Nenhum jogador no time</p>
                                        ) : (
                                            <div className="player-list">
                                                {selectedTime.jogadores?.map(j => (
                                                    <div key={j.id} className="player-item">
                                                        <span className="badge-sm">{j.laneLol}</span>
                                                        <span>{j.nickname}</span>
                                                        {selectedTime.capitao?.id === j.id && <span className="captain-badge">👑</span>}
                                                        <div className="player-actions">
                                                            {selectedTime.capitao?.id !== j.id && (
                                                                <button onClick={() => definirCapitao(selectedTime.id, j.id)} title="Definir como capitão">👑</button>
                                                            )}
                                                            <button onClick={() => removerJogadorDoTime(selectedTime.id, j.id)} title="Remover">❌</button>
                                                        </div>
                                                    </div>
                                                ))}
                                            </div>
                                        )}
                                    </div>

                                    <div className="detail-section">
                                        <h4>Adicionar Jogador Existente</h4>
                                        {jogadoresDisponiveis.length === 0 ? (
                                            <p className="empty">Nenhum jogador disponível</p>
                                        ) : (
                                            <div className="add-list">
                                                {jogadoresDisponiveis.map(j => (
                                                    <div key={j.id} className="add-item" onClick={() => adicionarJogadorAoTime(selectedTime.id, j.id)}>
                                                        <span className="badge-sm">{j.laneLol}</span>
                                                        <span>{j.nickname}</span>
                                                        <span className="add-icon">➕</span>
                                                    </div>
                                                ))}
                                            </div>
                                        )}
                                    </div>
                                </div>
                            ) : (
                                <div className="no-selection">
                                    <p>👈 Selecione um time para gerenciar</p>
                                </div>
                            )}
                        </div>
                    </div>
                )}

                {/* ==================== CAMPEONATOS ==================== */}
                {activeTab === 'campeonatos' && (
                    <div className="tab-content-full">
                        <div className="panel-left">
                            <div className="form-section">
                                <h3>Criar Campeonato</h3>
                                <form onSubmit={criarCampeonato}>
                                    <input type="text" placeholder="Nome" value={novoCampeonato.nome}
                                        onChange={(e) => setNovoCampeonato({ ...novoCampeonato, nome: e.target.value })} required />
                                    <input type="text" placeholder="Descrição" value={novoCampeonato.descricao}
                                        onChange={(e) => setNovoCampeonato({ ...novoCampeonato, descricao: e.target.value })} />
                                    <input type="number" placeholder="Limite Times" value={novoCampeonato.limiteMaximoTimes}
                                        onChange={(e) => setNovoCampeonato({ ...novoCampeonato, limiteMaximoTimes: Number(e.target.value) })} min="2" />
                                    <select
                                        value={novoCampeonato.edicaoId}
                                        onChange={(e) => setNovoCampeonato({ ...novoCampeonato, edicaoId: e.target.value })}
                                        className="select-edicao"
                                    >
                                        <option value="">📅 Sem edição vinculada</option>
                                        {edicoes.map(e => (
                                            <option key={e.id} value={e.id}>{e.nomeCompleto || e.nome} ({e.ano})</option>
                                        ))}
                                    </select>
                                    <button type="submit" className="btn btn-primary">Criar</button>

                                </form>
                            </div>
                            <div className="list-section">
                                <h3>Campeonatos</h3>
                                {campeonatos.length === 0 ? <p className="empty">Nenhum campeonato</p> : (
                                    <div className="data-list">
                                        {campeonatos.map(c => (
                                            <div key={c.id} className={`data-item ${selectedCampeonato?.id === c.id ? 'selected' : ''}`}
                                                onClick={() => setSelectedCampeonato(c)}>
                                                <div className="data-item-main">
                                                    <strong>{c.nome}</strong>
                                                    {c.edicaoNome && <span className="badge-edicao">📅 {c.edicaoNome}</span>}
                                                </div>
                                                <span className="badge-status">{c.status}</span>
                                                <button onClick={(e) => { e.stopPropagation(); deletarCampeonato(c.id); }} className="btn-delete">🗑️</button>
                                            </div>
                                        ))}
                                    </div>
                                )}
                            </div>
                        </div>

                        {/* Detalhes do Campeonato */}
                        <div className="panel-right">
                            {selectedCampeonato ? (
                                <div className="detail-panel">
                                    <h3>🏆 {selectedCampeonato.nome}</h3>
                                    <p className="detail-sub">Status: <span className="badge-status">{selectedCampeonato.status}</span></p>
                                    {selectedCampeonato.edicaoNome && (
                                        <p className="detail-sub">Edição: <span className="badge-edicao">{selectedCampeonato.edicaoNome}</span></p>
                                    )}
                                    <p className="detail-sub">Times: {selectedCampeonato.numeroTimesInscritos || 0}/{selectedCampeonato.limiteMaximoTimes || '∞'}</p>


                                    {/* Ações de Status */}
                                    <div className="action-buttons">
                                        {selectedCampeonato.status === 'RASCUNHO' && (
                                            <button onClick={() => abrirInscricoes(selectedCampeonato.id)} className="btn btn-sm">📝 Abrir Inscrições</button>
                                        )}
                                        {selectedCampeonato.status === 'INSCRICOES_ABERTAS' && (
                                            <button onClick={() => fecharInscricoes(selectedCampeonato.id)} className="btn btn-sm">🔒 Fechar Inscrições</button>
                                        )}
                                        {selectedCampeonato.status === 'INSCRICOES_ENCERRADAS' && (
                                            <button onClick={() => iniciarCampeonato(selectedCampeonato.id)} className="btn btn-sm btn-primary">🚀 Iniciar</button>
                                        )}
                                        {(selectedCampeonato.status === 'EM_ANDAMENTO' || selectedCampeonato.status === 'FINALIZADO') && (
                                            <Link to={`/campeonato/${selectedCampeonato.id}`} className="btn btn-sm btn-primary">📊 Ver Chaves</Link>
                                        )}
                                    </div>

                                    {/* Fases */}
                                    <div className="detail-section">
                                        <h4>Fases ({selectedCampeonato.fases?.length || 0})</h4>

                                        {/* Manual Matchmaking UI */}
                                        {manualMatchMode && (
                                            <div className="manual-match-panel">

                                                {/* UI para MATA-MATA */}
                                                {(targetFase.formato.startsWith('MATA_MATA') || targetFase.formato === 'LOSER_BRACKET') && (
                                                    <>
                                                        <h5>⚔️ Definir Confrontos Manualmente</h5>
                                                        <p className="hint-text">Selecione os times para cada confronto.</p>

                                                        {manualPairs.map((pair, idx) => (
                                                            <div key={idx} className="manual-pair-row">
                                                                <span className="pair-label">Jogo {idx + 1}</span>
                                                                <select
                                                                    value={pair.time1Id}
                                                                    onChange={(e) => updateManualPair(idx, 'time1Id', e.target.value)}
                                                                >
                                                                    <option value="">Time 1</option>
                                                                    {selectedCampeonato.timesParticipantes?.map(t => (
                                                                        <option key={t.id} value={t.id}>{t.nomeTime}</option>
                                                                    ))}
                                                                </select>
                                                                <span>VS</span>
                                                                <select
                                                                    value={pair.time2Id}
                                                                    onChange={(e) => updateManualPair(idx, 'time2Id', e.target.value)}
                                                                >
                                                                    <option value="">Time 2</option>
                                                                    {selectedCampeonato.timesParticipantes?.map(t => (
                                                                        <option key={t.id} value={t.id}>{t.nomeTime}</option>
                                                                    ))}
                                                                </select>
                                                                <button onClick={() => removeManualPair(idx)} className="btn-icon">❌</button>
                                                            </div>
                                                        ))}

                                                        <div className="manual-actions">
                                                            <button onClick={addManualPair} className="btn btn-sm btn-outline">+ Adicionar Jogo</button>
                                                            <div className="manual-submit-group">
                                                                <button onClick={() => setManualMatchMode(false)} className="btn btn-sm">Cancelar</button>
                                                                <button onClick={submitManualMatches} className="btn btn-sm btn-primary">Confirmar Jogos</button>
                                                            </div>
                                                        </div>
                                                    </>
                                                )}

                                                {/* UI para GRUPOS */}
                                                {targetFase.formato.startsWith('GRUPOS') && (
                                                    <>
                                                        <h5>🔢 Definir Grupos Manualmente</h5>
                                                        <p className="hint-text">Crie grupos e selecione os times participantes.</p>

                                                        <div className="manual-groups-container">
                                                            {manualGroups.map((group, idx) => (
                                                                <div key={idx} className="manual-group-card">
                                                                    <div className="manual-group-header">
                                                                        <input
                                                                            type="text"
                                                                            value={group.nome}
                                                                            onChange={(e) => updateGroupName(idx, e.target.value)}
                                                                            className="group-name-input"
                                                                        />
                                                                        <button onClick={() => removeManualGroup(idx)} className="btn-icon">❌</button>
                                                                    </div>
                                                                    <div className="manual-group-teams">
                                                                        {selectedCampeonato.timesParticipantes?.map(t => (
                                                                            <label key={t.id} className="team-checkbox-row">
                                                                                <input
                                                                                    type="checkbox"
                                                                                    checked={group.timesIds.includes(t.id)}
                                                                                    onChange={() => toggleTeamInGroup(idx, t.id)}
                                                                                />
                                                                                <span>{t.nomeTime}</span>
                                                                            </label>
                                                                        ))}
                                                                    </div>
                                                                    <div className="group-count">
                                                                        {group.timesIds.length} times selecionados
                                                                    </div>
                                                                </div>
                                                            ))}
                                                        </div>

                                                        <div className="manual-actions">
                                                            <button onClick={addManualGroup} className="btn btn-sm btn-outline">+ Adicionar Grupo</button>
                                                            <div className="manual-submit-group">
                                                                <button onClick={() => setManualMatchMode(false)} className="btn btn-sm">Cancelar</button>
                                                                <button onClick={submitManualMatches} className="btn btn-sm btn-primary">Confirmar Grupos</button>
                                                            </div>
                                                        </div>
                                                    </>
                                                )}
                                            </div>
                                        )}

                                        {selectedCampeonato.fases?.map((f, i) => (
                                            <div key={i} className="fase-item">
                                                <div className="fase-info">
                                                    <span>{f.nome}</span>
                                                    <span className="badge-sm">{f.formato}</span>
                                                </div>
                                                <div className="fase-actions">
                                                    <span className={f.finalizada ? 'status-done' : 'status-pending'}>
                                                        {f.finalizada ? '✅ Finalizada' : '⏳ Pendente'}
                                                    </span>
                                                    {!f.finalizada && !manualMatchMode &&
                                                        (f.formato.startsWith('MATA_MATA') || f.formato === 'LOSER_BRACKET' || f.formato.startsWith('GRUPOS')) && (
                                                            <button
                                                                onClick={() => startManualMatchmaking(f)}
                                                                className="btn-xs btn-outline"
                                                                title="Definir manualmente"
                                                            >
                                                                🛠️ Manual
                                                            </button>
                                                        )}
                                                </div>
                                            </div>
                                        ))}

                                        <div className="add-fase-form">
                                            <input type="text" placeholder="Nome da fase" value={novaFase.nome}
                                                onChange={(e) => setNovaFase({ ...novaFase, nome: e.target.value })} />
                                            <select value={novaFase.formato} onChange={(e) => setNovaFase({ ...novaFase, formato: e.target.value })}>
                                                {formatos.map(f => <option key={f} value={f}>{f}</option>)}
                                            </select>
                                            <input
                                                type="number"
                                                placeholder="Classificados (Total)"
                                                title="Quantos times avançam desta fase no total?"
                                                value={novaFase.classificadosNecessarios}
                                                onChange={(e) => setNovaFase({ ...novaFase, classificadosNecessarios: parseInt(e.target.value) })}
                                                min="1"
                                                style={{ width: '80px' }}
                                            />
                                            <button onClick={() => adicionarFase(selectedCampeonato.id)} className="btn btn-sm">+ Fase</button>
                                        </div>
                                    </div>

                                    {/* Inscrever Times */}
                                    {selectedCampeonato.status === 'INSCRICOES_ABERTAS' && (
                                        <div className="detail-section">
                                            <h4>Inscrever Time</h4>
                                            <div className="add-list">
                                                {timesDisponiveis.map(t => (
                                                    <div key={t.id} className="add-item" onClick={() => inscreverTime(selectedCampeonato.id, t.id)}>
                                                        <span>{t.nomeTime}</span>
                                                        <span className="add-icon">➕</span>
                                                    </div>
                                                ))}
                                            </div>
                                        </div>
                                    )}
                                </div>
                            ) : (
                                <div className="no-selection">
                                    <p>👈 Selecione um campeonato para gerenciar</p>
                                </div>
                            )}
                        </div>
                    </div>
                )}

                {/* ==================== EDIÇÕES ==================== */}
                {activeTab === 'edicoes' && (
                    <div className="tab-content">
                        <div className="form-section">
                            <h3>Criar Edição</h3>
                            <form onSubmit={criarEdicao}>
                                <div className="form-row">
                                    <div className="form-group">
                                        <label>Ano *</label>
                                        <input type="number" min="2021" max="2030" value={novaEdicao.ano}
                                            onChange={(e) => setNovaEdicao({ ...novaEdicao, ano: Number(e.target.value) })} required />
                                    </div>
                                    <div className="form-group">
                                        <label>Nº Edição</label>
                                        <input type="number" min="1" max="10" value={novaEdicao.numeroEdicao}
                                            onChange={(e) => setNovaEdicao({ ...novaEdicao, numeroEdicao: Number(e.target.value) })} />
                                    </div>
                                </div>
                                <div className="form-group">
                                    <label>Nome *</label>
                                    <input type="text" placeholder="Ex: CBPITU 2024" value={novaEdicao.nome}
                                        onChange={(e) => setNovaEdicao({ ...novaEdicao, nome: e.target.value })} required />
                                </div>
                                <div className="form-group">
                                    <label>Descrição</label>
                                    <input type="text" placeholder="Descrição da edição" value={novaEdicao.descricao}
                                        onChange={(e) => setNovaEdicao({ ...novaEdicao, descricao: e.target.value })} />
                                </div>
                                <button type="submit" className="btn btn-primary">Criar Edição</button>
                            </form>
                        </div>
                        <div className="list-section">
                            <h3>Edições Cadastradas</h3>
                            {edicoes.length === 0 ? <p className="empty">Nenhuma edição</p> : (
                                <div className="data-grid">
                                    {edicoes.map(e => (
                                        <div key={e.id} className="data-card edicao-card-admin">
                                            <div className="data-card-header">
                                                <strong>{e.nomeCompleto || e.nome}</strong>
                                                <span className="badge">{e.ano}</span>
                                            </div>
                                            <p>{e.descricao || 'Sem descrição'}</p>
                                            <div className="edicao-stats-admin">
                                                <span>📊 {e.numeroCampeonatos || 0} campeonatos</span>
                                                <span>👥 {e.numeroEscalacoes || 0} escalações</span>
                                            </div>
                                            <button onClick={() => deletarEdicao(e.id)} className="btn-delete">🗑️</button>
                                        </div>
                                    ))}
                                </div>
                            )}
                        </div>
                    </div>
                )}
            </div>

            <div className="admin-footer">
                <a href="/" className="btn btn-secondary">← Voltar</a>
                <button onClick={loadData} className="btn btn-outline">🔄 Recarregar</button>
            </div>
        </div>
    )
}

export default Admin
