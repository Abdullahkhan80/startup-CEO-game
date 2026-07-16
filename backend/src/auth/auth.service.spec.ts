import { Test, TestingModule } from '@nestjs/testing';
import { AuthService } from './auth.service';
import { UsersService } from '../users/users.service';
import { JwtService } from '@nestjs/jwt';
import * as bcrypt from 'bcrypt';

describe('AuthService', () => {
  let service: AuthService;
  let usersService: Partial<UsersService>;
  let jwtService: Partial<JwtService>;

  beforeEach(async () => {
    usersService = {
      findByEmail: jest.fn(),
      create: jest.fn(),
    };
    jwtService = {
      sign: jest.fn().mockReturnValue('mocked_token'),
    };

    const module: TestingModule = await Test.createTestingModule({
      providers: [
        AuthService,
        { provide: UsersService, useValue: usersService },
        { provide: JwtService, useValue: jwtService },
      ],
    }).compile();

    service = module.get<AuthService>( AuthService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });

  describe('validateUser', () => {
    it('should return user object if verification matches', async () => {
      const mockPassword = 'Password123';
      const hashedPassword = await bcrypt.hash(mockPassword, 10);
      const mockUser = { id: 'user-1', email: 'test@example.com', password: hashedPassword };

      (usersService.findByEmail as jest.Mock).mockResolvedValue(mockUser);

      const result = await service.validateUser('test@example.com', mockPassword);
      expect(result).toBeDefined();
      expect(result.email).toBe('test@example.com');
      expect(result.password).toBeUndefined(); // Password is stripped out
    });

    it('should return null if password hash comparison fails', async () => {
      const mockUser = { id: 'user-1', email: 'test@example.com', password: 'hashed_password' };
      (usersService.findByEmail as jest.Mock).mockResolvedValue(mockUser);

      const result = await service.validateUser('test@example.com', 'WrongPassword');
      expect(result).toBeNull();
    });
  });

  describe('login', () => {
    it('should sign JWT payload and return key properties', async () => {
      const mockUser = { id: 'user-id-123', email: 'test@example.com' };
      const result = await service.login(mockUser);

      expect(result.access_token).toBe('mocked_token');
      expect(result.user.id).toBe('user-id-123');
      expect(result.user.email).toBe('test@example.com');
    });
  });
});
