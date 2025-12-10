import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { jogadoresApi, timesApi, campeonatosApi } from '../../services/api'
import './Admin.css'

function Admin() {
    const [activeTab, setActiveTab] = useState('jogadores')
    const [jogadores, setJogadores] = useState([])
    const [times, setTimes] = useState([])
    const [campeonatos, setCampeonatos] = useState([])
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
    const [novoCampeonato, setNovoCampeonato] = useState({ nome: '', descricao: '', limiteMaximoTimes: 8 })
    const [novaFase, setNovaFase] = useState({ nome: '', formato: 'MATA_MATA', classificadosNecessarios: 1, rodadasTotais: 1 })

    const lanes = ['TOP', 'JUNGLE', 'MID', 'ADC', 'SUPPORT']
    const formatos = ['MATA_MATA', 'MATA_MATA_MD3', 'MATA_MATA_MD5', 'GRUPOS', 'GRUPOS_IDA_VOLTA', 'SISTEMA_SUICO', 'LOSER_BRACKET']

    useEffect(() => {
        loadData()
    }, [activeTab])

    const loadData = async () => {
        setLoading(true)
        setError(null)
        try {
            const [jogadoresData, timesData, campeonatosData] = await Promise.all([
                jogadoresApi.listar().catch(() => []),
                timesApi.listar().catch(() => []),
                campeonatosApi.listar().catch(() => [])
            ])
            setJogadores(jogadoresData || [])
            setTimes(timesData || [])
            setCampeonatos(campeonatosData || [])
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
            await campeonatosApi.criar(novoCampeonato)
            setNovoCampeonato({ nome: '', descricao: '', limiteMaximoTimes: 8 })
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
                                                <strong>{c.nome}</strong>
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
                                        {selectedCampeonato.fases?.map((f, i) => (
                                            <div key={i} className="fase-item">
                                                <span>{f.nome}</span>
                                                <span className="badge-sm">{f.formato}</span>
                                                <span className={f.finalizada ? 'status-done' : 'status-pending'}>
                                                    {f.finalizada ? '✅' : '⏳'}
                                                </span>
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
            </div>

            <div className="admin-footer">
                <a href="/" className="btn btn-secondary">← Voltar</a>
                <button onClick={loadData} className="btn btn-outline">🔄 Recarregar</button>
            </div>
        </div>
    )
}

export default Admin
