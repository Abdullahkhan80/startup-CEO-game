import { IsBoolean, IsNumber, IsString, IsOptional, Min } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';

export class CreateGameSaveDto {
  @ApiProperty({ example: 4, description: 'Current simulation calendar week' })
  @IsNumber()
  @Min(1)
  week!: number;

  @ApiProperty({ example: 180000.0, description: 'Current pre/post money valuation estimate' })
  @IsNumber()
  @Min(0)
  valuation!: number;

  @ApiProperty({ example: 54000.5, description: 'Liquid cash reserves' })
  @IsNumber()
  @Min(0)
  cash!: number;

  @ApiProperty({ example: 250, description: 'Total active user count' })
  @IsNumber()
  @Min(0)
  usersCount!: number;

  @ApiProperty({ example: 18.5, description: 'Product quality rating (0-100)' })
  @IsNumber()
  @Min(0)
  productQuality!: number;

  @ApiProperty({ example: '[]', description: 'JSON serialized list of hired employees' })
  @IsString()
  employeesJson!: string;

  @ApiProperty({ example: '[]', description: 'JSON string of historic news feed feed logs' })
  @IsString()
  newsFeedJson!: string;

  @ApiProperty({ example: '["North America"]', description: 'JSON array string of unlocked sectors/regions' })
  @IsString()
  unlockedRegionsJson!: string;

  @ApiProperty({ example: false, description: 'End-of-life bankruptcy/liquidation flag' })
  @IsBoolean()
  isGameOver!: boolean;

  @ApiProperty({ example: false, description: 'Whether the IPO exit threshold was achieved' })
  @IsBoolean()
  isIPOExited!: boolean;

  @ApiProperty({ example: 50000.0, description: 'Accumulated investment raises' })
  @IsNumber()
  @Min(0)
  totalInvestmentRaised!: number;

  @ApiProperty({ example: 12.5, description: 'Accumulated VC equity dilution percentages' })
  @IsNumber()
  @Min(0)
  totalVCDilution!: number;
}
