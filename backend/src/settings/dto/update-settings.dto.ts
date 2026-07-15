import { IsBoolean, IsNumber, IsOptional, Max, Min } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';

export class UpdateSettingsDto {
  @ApiProperty({ example: 0.8, required: false })
  @IsNumber()
  @IsOptional()
  @Min(0.0)
  @Max(1.0)
  volume?: number;

  @ApiProperty({ example: true, required: false })
  @IsBoolean()
  @IsOptional()
  vibration?: boolean;

  @ApiProperty({ example: true, required: false })
  @IsBoolean()
  @IsOptional()
  pushAlerts?: boolean;
}
