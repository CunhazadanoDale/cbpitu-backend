import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { timesApi } from '../../services/api'
import './Times.css'

function TimeCard({ time }) {
    return (
        <div className="time-card">
            <div className="time-card-image">
                <div className="image-placeholder">
                    <span>{time.nomeTime ? time.nomeTime.substring(0, 2).toUpperCase() : 'TM'}</span>
                </div>
            </div>
            <div className="time-card-content">
                <h3 className="time-name">{time.nomeTime}</h3>
                <p className="time-titles">
                    {time.trofeus > 0 ? `🏆 ${time.trofeus} Títulos` : 'Em busca da glória'}
                </p>
                <div className="time-players">
                    <span>{time.jogadores?.length || 0} jogadores</span>
                </div>
            </div>
        </div>
    )
}

function Times() {
    const [times, setTimes] = useState([])
    const [loading, setLoading] = useState(true)

    useEffect(() => {
        const fetchTopTimes = async () => {
            try {
                const data = await timesApi.listarTop()
                setTimes(data)
            } catch (error) {
                console.error("Erro ao carregar top times:", error)
            } finally {
                setLoading(false)
            }
        }

        fetchTopTimes()
    }, [])

    return (
        <section id="times" className="section times">
            <div className="container">
                <div className="section-header">
                    <span className="section-tag">Participantes</span>
                    <h2 className="section-title">Conheça os <span className="gradient-text">Top Times</span></h2>
                    <p className="section-description">Os esquadrões que dominam a glória do CBPitu</p>
                </div>

                {loading ? (
                    <div className="loading-spinner"></div>
                ) : (
                    <div className="times-grid">
                        {times.map(time => (
                            <TimeCard key={time.id} time={time} />
                        ))}
                    </div>
                )}

                <div className="times-cta">
                    <Link to="/times" className="btn btn-outline">Ver Todos os Times</Link>
                </div>
            </div>
        </section>
    )
}

export default Times
