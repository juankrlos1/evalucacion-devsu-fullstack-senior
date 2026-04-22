export interface Cliente {
  customerId: string;
  name: string;
  gender: string;
  age: number;
  identification: string;
  address: string;
  phone: string;
  password: string;
  active: boolean;
}

export interface Cuenta {
  accountNumber: string;
  accountType: string;
  initialBalance: number;
  availableBalance?: number;
  active: boolean;
  customerId: string;
  customerName?: string;
}

export interface Movimiento {
  movementId?: string;
  date?: string;
  movementType?: string;
  accountNumber: string;
  value: number;
  balance?: number;
}

export interface ReporteItem {
  date: string;
  customer: string;
  accountNumber: string;
  accountType: string;
  initialBalance: number;
  active: boolean;
  movement: number;
  availableBalance: number;
}

export interface ApiResponse<T> {
  timestamp: string;
  success: boolean;
  message: string;
  data: T;
  errors: any;
}

export interface AuthRequest {
  identification: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  customerId: string;
  name: string;
  role: string;
}
