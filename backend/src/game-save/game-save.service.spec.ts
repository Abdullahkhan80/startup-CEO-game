import { Test, TestingModule } from '@nestjs/testing';
import { GameSaveService } from './game-save.service';
import { PrismaService } from '../prisma/prisma.service';

describe('GameSaveService', () => {
  let service: GameSaveService;
  let prisma: Partial<PrismaService>;

  beforeEach(async () => {
    prisma = {
      gameSave: {
        findFirst: jest.fn(),
        create: jest.fn(),
        update: jest.fn(),
        deleteMany: jest.fn(),
      } as any,
    };

    const module: TestingModule = await Test.createTestingModule({
      providers: [
        GameSaveService,
        { provide: PrismaService, useValue: prisma },
      ],
    }).compile();

    service = module.get<GameSaveService>( GameSaveService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });

  describe('getLatestSave', () => {
    it('should return mapped save state when entry exists', async () => {
      const mockSave = {
        id: 'save-1',
        userId: 'user-1',
        week: 5,
        usersCount: BigInt(500),
        valuation: 220000.0,
      };

      (prisma.gameSave!.findFirst as jest.Mock).mockResolvedValue(mockSave);

      const result = await service.getLatestSave('user-1');
      expect(result).toBeDefined();
      expect(result!.usersCount).toBe(500); // BigInt to Number mapping check
    });

    it('should throw NotFoundException if query is empty', async () => {
      (prisma.gameSave!.findFirst as jest.Mock).mockResolvedValue(null);

      await expect(service.getLatestSave('user-1')).rejects.toThrow();
    });
  });
});
