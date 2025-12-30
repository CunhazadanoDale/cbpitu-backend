const API_BASE_URL = 'http://localhost:8080/api';

/**
 * Função base para fazer requisições HTTP
 */
async function request(endpoint, options = {}) {
    const url = `${API_BASE_URL}${endpoint}`;

    const config = {
        headers: {
            'Content-Type': 'application/json',
            ...options.headers,
        },
        ...options,
    };

    try {
        const response = await fetch(url, config);

        // Se não houver conteúdo (204 No Content)
        if (response.status === 204) {
            return null;
        }

        const data = await response.json();

        if (!response.ok) {
            throw {
                status: response.status,
                ...data
            };
        }

        return data;
    } catch (error) {
        console.error('API Error:', error);
        throw error;
    }
}

// ==================== Jogadores ====================

export const jogadoresApi = {
    listar: () => request('/jogadores'),

    buscarPorId: (id) => request(`/jogadores/${id}`),

    buscarDetalhes: (id) => request(`/jogadores/${id}/detalhes`),

    buscarPorLane: (lane) => request(`/jogadores/lane/${lane}`),

    buscarSemTime: () => request('/jogadores/sem-time'),

    criar: (jogador) => request('/jogadores', {
        method: 'POST',
        body: JSON.stringify(jogador),
    }),

    atualizar: (id, jogador) => request(`/jogadores/${id}`, {
        method: 'PUT',
        body: JSON.stringify(jogador),
    }),

    deletar: (id) => request(`/jogadores/${id}`, {
        method: 'DELETE',
    }),
};

// ==================== Times ====================

export const timesApi = {
    listar: () => request('/times'),

    listarPaginado: (page = 0, size = 12) => request(`/times/paginado?page=${page}&size=${size}&sort=trofeus,desc`),

    listarTop: () => request('/times/top'),

    buscarDetalhes: (id) => request(`/times/${id}/detalhes`),

    buscarPorId: (id) => request(`/times/${id}`),

    criar: (dados) => request('/times', {
        method: 'POST',
        body: JSON.stringify(dados)
    }),

    atualizar: (id, dados) => request(`/times/${id}`, {
        method: 'PUT',
        body: JSON.stringify(dados)
    }),

    deletar: (id) => request(`/times/${id}`, {
        method: 'DELETE'
    }),

    // Gerenciamento de jogadores
    adicionarJogador: (timeId, jogadorId) => request(`/times/${timeId}/jogadores/${jogadorId}`, {
        method: 'POST'
    }),

    removerJogador: (timeId, jogadorId) => request(`/times/${timeId}/jogadores/${jogadorId}`, {
        method: 'DELETE'
    }),

    definirCapitao: (timeId, jogadorId) => request(`/times/${timeId}/capitao/${jogadorId}`, {
        method: 'PUT'
    })
};

// ==================== Campeonatos ====================

export const campeonatosApi = {
    listar: () => request('/campeonatos'),

    listarAtivos: () => request('/campeonatos/ativos'),

    buscarPorId: (id) => request(`/campeonatos/${id}`),

    criar: (campeonato) => request('/campeonatos', {
        method: 'POST',
        body: JSON.stringify(campeonato),
    }),

    deletar: (id) => request(`/campeonatos/${id}`, {
        method: 'DELETE',
    }),

    abrirInscricoes: (id) => request(`/campeonatos/${id}/abrir-inscricoes`, {
        method: 'POST',
    }),

    fecharInscricoes: (id) => request(`/campeonatos/${id}/fechar-inscricoes`, {
        method: 'POST',
    }),

    iniciar: (id) => request(`/campeonatos/${id}/iniciar`, {
        method: 'POST',
    }),

    avancarFase: (id) => request(`/campeonatos/${id}/avancar-fase`, {
        method: 'POST',
    }),

    inscreverTime: (campeonatoId, timeId) => request(`/campeonatos/${campeonatoId}/times/${timeId}`, {
        method: 'POST',
    }),

    removerTime: (campeonatoId, timeId) => request(`/campeonatos/${campeonatoId}/times/${timeId}`, {
        method: 'DELETE',
    }),

    adicionarFase: (id, fase) => request(`/campeonatos/${id}/fases`, {
        method: 'POST',
        body: JSON.stringify(fase),
    }),

    criarConfrontosManuais: (id, faseId, confrontos) => request(`/campeonatos/${id}/fases/${faseId}/partidas/manual`, {
        method: 'POST',
        body: JSON.stringify(confrontos),
    }),
};

// ==================== Partidas ====================

export const partidasApi = {
    listar: () => request('/partidas'),

    buscarPorId: (id) => request(`/partidas/${id}`),

    listarPorCampeonato: (campeonatoId) => request(`/partidas/campeonato/${campeonatoId}`),

    listarPorFase: (faseId) => request(`/partidas/fase/${faseId}`),

    listarPorGrupo: (grupoId) => request(`/partidas/grupo/${grupoId}`),

    listarPorTime: (timeId) => request(`/partidas/time/${timeId}`),

    listarPendentes: (campeonatoId) => request(`/partidas/campeonato/${campeonatoId}/pendentes`),

    registrarResultado: (id, resultado) => request(`/partidas/${id}/resultado`, {
        method: 'PUT',
        body: JSON.stringify(resultado),
    }),
};

// ==================== Edições ====================

export const edicoesApi = {
    listar: () => request('/edicoes'),

    buscarPorId: (id) => request(`/edicoes/${id}`),

    buscarPorAno: (ano) => request(`/edicoes/ano/${ano}`),

    criar: (edicao) => request('/edicoes', {
        method: 'POST',
        body: JSON.stringify(edicao),
    }),

    atualizar: (id, edicao) => request(`/edicoes/${id}`, {
        method: 'PUT',
        body: JSON.stringify(edicao),
    }),

    deletar: (id) => request(`/edicoes/${id}`, {
        method: 'DELETE',
    }),

    buscarEscalacoes: (id) => request(`/edicoes/${id}/escalacoes`),
};

// ==================== Escalações ====================

export const escalacoesApi = {
    listar: () => request('/escalacoes'),

    buscarPorId: (id) => request(`/escalacoes/${id}`),

    buscarHistoricoTime: (timeId) => request(`/escalacoes/time/${timeId}`),

    criar: (escalacao) => request('/escalacoes', {
        method: 'POST',
        body: JSON.stringify(escalacao),
    }),

    atualizar: (id, escalacao) => request(`/escalacoes/${id}`, {
        method: 'PUT',
        body: JSON.stringify(escalacao),
    }),

    deletar: (id) => request(`/escalacoes/${id}`, {
        method: 'DELETE',
    }),
};

export default {
    jogadores: jogadoresApi,
    times: timesApi,
    campeonatos: campeonatosApi,
    partidas: partidasApi,
    edicoes: edicoesApi,
    escalacoes: escalacoesApi,
};
