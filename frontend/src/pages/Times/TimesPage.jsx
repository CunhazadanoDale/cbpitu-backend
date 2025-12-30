import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { timesApi } from '../../services/api'
import TeamProfileModal from '../../components/TeamProfileModal/TeamProfileModal'
import './TimesPage.css'

function TimesPage() {
    const [times, setTimes] = useState([])
    const [loading, setLoading] = useState(true)
    const [page, setPage] = useState(0)
    const [totalPages, setTotalPages] = useState(0)
    const [selectedTimeId, setSelectedTimeId] = useState(null)

    useEffect(() => {
        carregarTimes()
    }, [page])

    const carregarTimes = async () => {
        setLoading(true)
        try {
            const data = await timesApi.listarPaginado(page)
            setTimes(data.content)
            setTotalPages(data.totalPages)
        } catch (error) {
            console.error("Erro ao carregar times:", error)
        } finally {
            setLoading(false)
        }
    }

    const handleNextPage = () => {
        if (page < totalPages - 1) setPage(page + 1)
    }

    const handlePrevPage = () => {
        if (page > 0) setPage(page - 1)
    }

    return (
        <div className="times-page">
            <TeamProfileModal
                timeId={selectedTimeId}
                onClose={() => setSelectedTimeId(null)}
            />

            <header className="page-header">
                <Link to="/" className="back-link" style={{ position: 'absolute', left: '24px', top: '24px' }}>← Voltar ao Início</Link>
                <h1 className="page-title">Todos os Times</h1>
                <p className="page-description">Explore os históricos e conquistas de todas as equipes do CBPitu.</p>
            </header>

            <div className="times-content">
                {loading ? (
                    <div className="loading-container">
                        <div className="loading-spinner"></div>
                        <p>Carregando times...</p>
                    </div>
                ) : (
                    <>
                        <div className="times-full-grid">
                            {times.map(time => (
                                <div
                                    key={time.id}
                                    className="time-full-card"
                                    onClick={() => setSelectedTimeId(time.id)}
                                >
                                    <div className="time-full-header">
                                        <div className="time-avatar-placeholder">
                                            {time.nomeTime ? time.nomeTime.substring(0, 2).toUpperCase() : 'TM'}
                                        </div>
                                    </div>
                                    <div className="time-full-body">
                                        <h3 className="time-full-name">{time.nomeTime}</h3>
                                        <div className="time-full-trophies">
                                            {time.trofeus > 0 ? `🏆 ${time.trofeus} Títulos` : '-'}
                                        </div>
                                        <div className="time-full-meta">
                                            Clique para ver detalhes
                                        </div>
                                    </div>
                                </div>
                            ))}
                        </div>

                        {totalPages > 1 && (
                            <div className="pagination-controls">
                                <button
                                    className="pagination-btn"
                                    onClick={handlePrevPage}
                                    disabled={page === 0}
                                >
                                    Anterior
                                </button>
                                <span className="pagination-info">Página {page + 1} de {totalPages}</span>
                                <button
                                    className="pagination-btn"
                                    onClick={handleNextPage}
                                    disabled={page >= totalPages - 1}
                                >
                                    Próxima
                                </button>
                            </div>
                        )}
                    </>
                )}
            </div>
        </div>
    )
}

export default TimesPage
