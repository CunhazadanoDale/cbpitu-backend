import { useState, useEffect } from 'react'
import { jogadoresApi } from '../../services/api'
import './PlayerProfileModal.css'

function PlayerProfileModal({ jogadorId, onClose }) {
    const [jogador, setJogador] = useState(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState(null)

    useEffect(() => {
        const fetchDetails = async () => {
            try {
                setLoading(true)
                const data = await jogadoresApi.buscarDetalhes(jogadorId)
                setJogador(data)
            } catch (err) {
                console.error("Erro ao buscar detalhes do jogador:", err)
                setError("Não foi possível carregar as informações do jogador.")
            } finally {
                setLoading(false)
            }
        }

        if (jogadorId) {
            fetchDetails()
        }
    }, [jogadorId])

    if (!jogadorId) return null

    return (
        <div className="modal-overlay" onClick={(e) => {
            e.stopPropagation()
            onClose()
        }}>
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
                                {jogador.nickname.charAt(0).toUpperCase()}
                            </div>
                            <h2 className="profile-nick">{jogador.nickname}</h2>
                            {jogador.nomeReal && <p className="profile-realname">{jogador.nomeReal}</p>}
                            <span className="profile-lane">{jogador.laneLol}</span>
                        </div>

                        <div className="profile-stats">
                            <div className="stat-box">
                                <span className="stat-number">{jogador.titulos || 0}</span>
                                <span className="stat-label">Títulos</span>
                            </div>
                            <div className="stat-box">
                                <span className="stat-number">{jogador.historico?.length || 0}</span>
                                <span className="stat-label">Edições</span>
                            </div>
                        </div>

                        <div className="profile-history">
                            <h3>Histórico de Times</h3>
                            {jogador.historico && jogador.historico.length > 0 ? (
                                <div className="history-list">
                                    {jogador.historico.map((item, index) => (
                                        <div key={index} className="history-item">
                                            <div className="history-info">
                                                <span className="history-team">
                                                    {item.nomeTime}
                                                    {item.isCapitao && <span className="captain-mark" title="Capitão">👑</span>}
                                                </span>
                                                <span className="history-edition">{item.nomeEdicao} ({item.anoEdicao})</span>
                                            </div>
                                            {/* Futuro: Mostrar ícone de troféu se foi campeão nessa edição específica */}
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
        </div>
    )
}

export default PlayerProfileModal
