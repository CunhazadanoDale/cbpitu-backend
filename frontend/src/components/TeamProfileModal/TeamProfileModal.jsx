import { useState, useEffect } from 'react'
import { timesApi } from '../../services/api'
import PlayerProfileModal from '../PlayerProfileModal/PlayerProfileModal'
import './TeamProfileModal.css'

function TeamProfileModal({ timeId, onClose }) {
    const [time, setTime] = useState(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState(null)
    const [selectedPlayerId, setSelectedPlayerId] = useState(null)

    useEffect(() => {
        const fetchDetails = async () => {
            try {
                setLoading(true)
                const data = await timesApi.buscarDetalhes(timeId)
                setTime(data)
            } catch (err) {
                console.error("Erro ao buscar detalhes do time:", err)
                setError("Não foi possível carregar as informações do time.")
            } finally {
                setLoading(false)
            }
        }

        if (timeId) {
            fetchDetails()
        }
    }, [timeId])

    if (!timeId) return null

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal-content" onClick={e => e.stopPropagation()}>
                <button className="modal-close" onClick={onClose}>&times;</button>

                {loading ? (
                    <div className="profile-header">
                        <p>Carregando perfil...</p>
                    </div>
                ) : error ? (
                    <div className="profile-header">
                        <p className="error-text">{error}</p>
                    </div>
                ) : (
                    <>
                        <div className="profile-header">
                            <div className="profile-avatar">
                                🛡️
                            </div>
                            <h2 className="profile-nick">{time.nomeTime}</h2>
                            {time.capitaoAtual && (
                                <p className="profile-realname">
                                    Capitão Atual: <span style={{ color: '#ffd700' }}>{time.capitaoAtual.nickname}</span>
                                </p>
                            )}
                        </div>

                        <div className="profile-stats">
                            <div className="stat-box">
                                <span className="stat-number">{time.trofeus || 0}</span>
                                <span className="stat-label">Troféus</span>
                            </div>
                            <div className="stat-box">
                                <span className="stat-number">{time.historico?.length || 0}</span>
                                <span className="stat-label">Participações</span>
                            </div>
                        </div>

                        <div className="profile-history">
                            <h3>Histórico em Edições</h3>
                            {time.historico && time.historico.length > 0 ? (
                                <div className="history-list">
                                    {time.historico.map((item, index) => (
                                        <div key={index} className="history-item">
                                            <div className="history-header">
                                                <span className="history-edition">{item.nomeEdicao}</span>
                                                <span className="history-edition-year">{item.anoEdicao}</span>
                                            </div>
                                            <div className="history-roster">
                                                {item.jogadores && item.jogadores.map(jogador => (
                                                    <span
                                                        key={jogador.id}
                                                        className={`roster-player ${item.capitaoNaEdicao?.id === jogador.id ? 'captain' : ''} clickable-player`}
                                                        title={item.capitaoNaEdicao?.id === jogador.id ? 'Capitão na edição' : 'Clique para ver perfil'}
                                                        onClick={() => setSelectedPlayerId(jogador.id)}
                                                    >
                                                        {item.capitaoNaEdicao?.id === jogador.id && '👑 '}
                                                        {jogador.nickname}
                                                    </span>
                                                ))}
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            ) : (
                                <p className="empty-text">Nenhum histórico encontrado.</p>
                            )}
                        </div>
                    </>
                )}
            </div>
            {selectedPlayerId && (
                <PlayerProfileModal
                    jogadorId={selectedPlayerId}
                    onClose={() => setSelectedPlayerId(null)}
                />
            )}
        </div>
    )
}

export default TeamProfileModal
