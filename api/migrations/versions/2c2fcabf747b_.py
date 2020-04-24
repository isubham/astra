"""empty message

Revision ID: 2c2fcabf747b
Revises: 688f98c398ff
Create Date: 2020-04-24 08:17:31.318683

"""
from alembic import op
import sqlalchemy as sa


# revision identifiers, used by Alembic.
revision = '2c2fcabf747b'
down_revision = '688f98c398ff'
branch_labels = None
depends_on = None


def upgrade():
    # ### commands auto generated by Alembic - please adjust! ###
    op.create_table('Activity',
    sa.Column('id', sa.Integer(), nullable=False),
    sa.Column('user_id', sa.Integer(), nullable=True),
    sa.Column('when', sa.DateTime(), nullable=True),
    sa.Column('type', sa.Enum('check_in', 'check_out', name='activity_type'), nullable=True),
    sa.Column('location', sa.String(), nullable=True),
    sa.PrimaryKeyConstraint('id'),
    sa.UniqueConstraint('user_id')
    )
    op.create_table('People',
    sa.Column('id', sa.Integer(), nullable=False),
    sa.Column('first_name', sa.String(), nullable=True),
    sa.Column('last_name', sa.String(), nullable=True),
    sa.Column('profile_pic', sa.String(), nullable=True),
    sa.Column('id_front', sa.String(), nullable=True),
    sa.Column('id_back', sa.String(), nullable=True),
    sa.Column('father_name', sa.String(), nullable=True),
    sa.Column('username', sa.String(), nullable=True),
    sa.Column('created', sa.DateTime(), nullable=True),
    sa.Column('modified', sa.DateTime(), nullable=True),
    sa.PrimaryKeyConstraint('id')
    )
    # ### end Alembic commands ###


def downgrade():
    # ### commands auto generated by Alembic - please adjust! ###
    op.drop_table('People')
    op.drop_table('Activity')
    # ### end Alembic commands ###
