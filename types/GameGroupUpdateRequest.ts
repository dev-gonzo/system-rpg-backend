/**
 * Tipagem para atualização de grupos de jogo
 * Baseada no DTO GameGroupUpdateRequest do backend
 */
export interface GameGroupUpdateRequest {
  /** Nome da campanha (obrigatório, 3-100 caracteres) */
  campaignName: string;

  /** Descrição detalhada (opcional, máximo 500 caracteres) */
  description?: string;

  /** Sistema de jogo (obrigatório, 2-50 caracteres) */
  gameSystem: string;

  /** Mundo/cenário do jogo (opcional, máximo 100 caracteres) */
  settingWorld?: string;

  /** Descrição curta (obrigatório, 3-100 caracteres) */
  shortDescription: string;

  /** Visibilidade do grupo */
  visibility: 'PUBLIC' | 'FRIENDS' | 'PRIVATE';

  /** Regra de acesso */
  accessRule: 'FREE' | 'FRIENDS' | 'APPROVAL';

  /** Modalidade do jogo */
  modality: 'ONLINE' | 'PRESENCIAL';

  /** Número mínimo de jogadores (opcional) */
  minPlayers?: number;

  /** Número máximo de jogadores (opcional) */
  maxPlayers?: number;

  /** País (opcional, máximo 100 caracteres) */
  country?: string;

  /** Estado (opcional, máximo 100 caracteres) */
  state?: string;

  /** Cidade (opcional, máximo 100 caracteres) */
  city?: string;

  /** Conteúdo e temas (opcional, máximo 500 caracteres) */
  themesContent?: string;

  /** Pontualidade e frequência (opcional, máximo 500 caracteres) */
  punctualityAttendance?: string;

  /** Regras da casa (opcional, máximo 500 caracteres) */
  houseRules?: string;

  /** Expectativas comportamentais (opcional, máximo 500 caracteres) */
  behavioralExpectations?: string;
}

/**
 * Enums auxiliares para melhor tipagem
 */
export enum GameGroupVisibility {
  PUBLIC = 'PUBLIC',
  FRIENDS = 'FRIENDS',
  PRIVATE = 'PRIVATE'
}

export enum GameGroupAccessRule {
  FREE = 'FREE',
  FRIENDS = 'FRIENDS',
  APPROVAL = 'APPROVAL'
}

export enum GameGroupModality {
  ONLINE = 'ONLINE',
  PRESENCIAL = 'PRESENCIAL'
}

/**
 * Versão com enums para melhor validação em tempo de compilação
 */
export interface GameGroupUpdateRequestTyped {
  campaignName: string;
  description?: string;
  gameSystem: string;
  settingWorld?: string;
  shortDescription: string;
  visibility: GameGroupVisibility;
  accessRule: GameGroupAccessRule;
  modality: GameGroupModality;
  minPlayers?: number;
  maxPlayers?: number;
  country?: string;
  state?: string;
  city?: string;
  themesContent?: string;
  punctualityAttendance?: string;
  houseRules?: string;
  behavioralExpectations?: string;
}